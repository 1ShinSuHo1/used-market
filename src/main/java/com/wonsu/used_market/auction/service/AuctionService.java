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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final AuctionProperties auctionProperties;

    //경매 생성
    @Transactional
    public AuctionResponseDto createAuction(CreateAuctionRequestDto dto, User currentUser){
        //경매 비정상하게 생성시 오류발생
        if(dto.getStartAt() == null || dto.getEndAt() == null || !dto.getEndAt().isAfter(dto.getStartAt())){
            throw new BusinessException(ErrorCode.INVALID_AUCTION_TIME);
        }

        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        //소유자 검증
        if(!product.getSeller().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN){
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        //상품당 1개 경매 제한
        if(product.getAuction() != null){
            throw new BusinessException(ErrorCode.AUCTION_ALREADY_EXISTS);
        }

        Auction auction = Auction.builder()
                .startPrice(dto.getStartPrice())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .build();

        auction.assignToProduct(product);
        auctionRepository.save(auction);

        return AuctionResponseDto.from(auction);

    }

    //입찰
    //자유 입찰
    @Transactional
    public  AuctionResponseDto placeFreeBid(PlaceFreeBidRequestDto dto, User bidder){
        return null;
    }

    // 경매 상태 검증하기 위해 사용
    private Auction getActiveAuction(Long auctionId){
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));

        if(auction.getStatus() != AuctionStatus.ACTIVE){
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        return auction;
    }


    // 현재 시간이 경매 진행 구간내인지 검증 하는 메서드

}

