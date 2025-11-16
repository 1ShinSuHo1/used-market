package com.wonsu.used_market.auction.service;

import com.wonsu.used_market.auction.config.AuctionProperties;
import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.auction.domain.AuctionStatus;
import com.wonsu.used_market.auction.dto.AuctionResponseDto;
import com.wonsu.used_market.auction.dto.CreateAuctionRequestDto;
import com.wonsu.used_market.auction.dto.PlaceFreeBidRequestDto;
import com.wonsu.used_market.auction.repository.AuctionRepository;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final RedisTemplate<String,Object> redisTemplate;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AuctionProperties auctionProperties;
    private final AuctionStompService auctionStompService;

    //경매 생성
    @Transactional
    public AuctionResponseDto createAuction(CreateAuctionRequestDto dto, User currentUser){

        // dto 검증
        if (dto.getStartAt() == null || dto.getEndAt() == null || !dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new BusinessException(ErrorCode.INVALID_AUCTION_TIME);
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 권한 체크
        if (!product.getSeller().getId().equals(currentUser.getId())
                && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        // 여기서 공통 메서드 호출만 함
        Auction auction = createAuctionForProduct(
                product,
                dto.getStartPrice(),
                dto.getEndAt()
        );

        return AuctionResponseDto.from(auction);
    }

    //입찰
    //자유 입찰
    @Transactional
    public  AuctionResponseDto placeFreeBid(PlaceFreeBidRequestDto dto, User bidder){
        Auction auction = getActiveAuction(dto.getAuctionId());
        LocalDateTime now = LocalDateTime.now();

        //경매기간 내인가 검사
        if (now.isBefore(auction.getStartAt()) || now.isAfter(auction.getEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_AUCTION_TIME);
        }

        int bidAmount = dto.getBidAmount();
        int currentPrice = auction.getCurrentPrice();
        int freeMin = auctionProperties.getFreeMin();

        // 금액 검증
        if (bidAmount <= currentPrice || bidAmount - currentPrice < freeMin) {
            throw new BusinessException(ErrorCode.BID_TOO_LOW);
        }

        // 경매 금액 갱신 및 낙찰자 설정
        auction.placeBid(bidder, bidAmount);
        auctionRepository.save(auction);

        // 실시간 반영
        auctionStompService.placeBid(auction.getId(), bidder.getNickname(), bidAmount);

        log.info("[FREE BID] auctionId={}, bidder={}, bid={}", auction.getId(), bidder.getNickname(), bidAmount);

        return AuctionResponseDto.from(auction);
    }

    //자동입찰
    @Transactional
    public AuctionResponseDto placeQuickBid(Long auctionId, User bidder){
        Auction auction = getActiveAuction(auctionId);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(auction.getStartAt()) || now.isAfter(auction.getEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_AUCTION_TIME);
        }

        int currentPrice = auction.getCurrentPrice();
        int nextBid;

        //단위 계산
        if (currentPrice < auctionProperties.getQuickBid().getThreshold()) {
            nextBid = currentPrice + auctionProperties.getQuickBid().getUnder();
        } else {
            nextBid = currentPrice + auctionProperties.getQuickBid().getOver();
        }

        auction.placeBid(bidder, nextBid);
        auctionRepository.save(auction);

        //실시간 전송
        auctionStompService.placeBid(auction.getId(), bidder.getNickname(), nextBid);

        log.info("[QUICK BID] auctionId={}, bidder={}, bid={}", auction.getId(), bidder.getNickname(), nextBid);

        return AuctionResponseDto.from(auction);
    }

    //경매취소
    @Transactional
    public void  cancelAuction(Long auctionId, User currentUser){
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

        //권한검사
        boolean isSeller = auction.getProduct().getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isSeller && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        //입찰 여부 확인
        boolean hasBid = !auction.getBids().isEmpty();
        if (hasBid && !isAdmin) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_WITH_BIDS);
        }

        auction.cancelAuction();
        auctionRepository.save(auction);

        log.info("[AUCTION CANCELLED] auctionId={} by={}", auction.getId(), currentUser.getNickname());
    }

    //단일 경매 조회
    public AuctionResponseDto getAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        return AuctionResponseDto.from(auction);
    }

    //전체 경매 조회
    public Page<AuctionResponseDto> getAllAuctions(Pageable pageable) {
        List<Auction> auctions = auctionRepository.findAllWithAssociations(pageable);
        long total = auctionRepository.countAll();

        List<AuctionResponseDto> dtoList = auctions.stream()
                .map(AuctionResponseDto::from)
                .toList();

        return new PageImpl<>(dtoList, pageable, total);
    }



    // 경매 상태 검증하기 위해 사용
    private Auction getActiveAuction(Long auctionId){
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        return auction;
    }

    @Transactional
    public Auction createAuctionForProduct(Product product,
                                           Integer auctionStartPrice,
                                           LocalDateTime auctionEndAt) {
        log.error("=== DEBUG AUCTION TIME ===");
        log.error("Raw auctionEndAt = {}", auctionEndAt);
        log.error("Now(KST) = {}", LocalDateTime.now());


        if (auctionEndAt == null || !auctionEndAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_AUCTION_TIME);
        }

        // 상품당 1개 경매 제한
        if (product.getAuction() != null) {
            throw new BusinessException(ErrorCode.AUCTION_ALREADY_EXISTS);
        }

        int startPrice = (auctionStartPrice != null)
                ? auctionStartPrice
                : product.getPrice(); // 없으면 상품 가격 사용

        LocalDateTime now = LocalDateTime.now();

        Auction auction = Auction.builder()
                .startPrice(startPrice)
                .startAt(now)
                .endAt(auctionEndAt)
                .build();

        auction.assignToProduct(product); // product.assignAuction(this)까지 처리
        auctionRepository.save(auction);

        // 🔁 여기서 Redis TTL 세팅도 같이 처리
        try {
            long ttlSeconds = Duration.between(now, auctionEndAt).getSeconds() + 600;
            if (ttlSeconds > 0) {
                String key = "auction:" + auction.getId();
                redisTemplate.opsForHash().put(key, "currentPrice", startPrice);
                redisTemplate.opsForHash().put(key, "winner", null);
                redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
                log.info("[AUCTION TTL SET] key={} TTL={}s (≈{}분)", key, ttlSeconds, ttlSeconds / 60);
            }
        } catch (Exception e) {
            log.warn("Redis TTL 설정 실패 (auctionId={})", auction.getId(), e);
        }

        return auction;
    }




}

