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
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.Role;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
class AuctionServiceTest    {
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionProperties auctionProperties;

    private User seller;
    private User bidder;
    private Product product;

    @BeforeEach
    void setUp() {
        seller = userRepository.save(User.builder()
                .email("seller@test.com")
                .nickname("seller")
                .role(Role.USER)
                .build());

        bidder = userRepository.save(User.builder()
                .email("bidder@test.com")
                .nickname("bidder")
                .role(Role.USER)
                .build());

        product = productRepository.save(Product.builder()
                .seller(seller)
                .category(Category.NOTEBOOK)
                .maker("Apple")
                .title("MacBook Air M2")
                .saleType(SaleType.AUCTION)
                .price(1000000)
                .description("테스트용 상품")
                .build());
    }

    //경매 생성 성공
    @Test
    public void createAuction() throws Exception{
        // given
        CreateAuctionRequestDto dto = new CreateAuctionRequestDto(
                product.getId(),
                1000000,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(5)
        );

        // when
        AuctionResponseDto response = auctionService.createAuction(dto, seller);

        // then
        assertThat(response.getProductId()).isEqualTo(product.getId());
        assertThat(response.getCurrentPrice()).isEqualTo(1000000);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    //자유 입찰 성공
    @Test
    public void placeFreeBid() throws Exception{
        // given
        productRepository.save(product);

        Auction auction = Auction.builder()
                .startPrice(1000000)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(5))
                .build();

        auction.assignToProduct(product);
        auctionRepository.save(auction);

        PlaceFreeBidRequestDto dto = new PlaceFreeBidRequestDto(auction.getId(), 1010000);

        // when
        AuctionResponseDto response = auctionService.placeFreeBid(dto, bidder);

        // then
        assertThat(response.getCurrentPrice()).isEqualTo(1010000);
        assertThat(response.getWinnerNickname()).isEqualTo(bidder.getNickname());
    }

    //빠른 입찰 성공
    @Test
    public void placeQuickBid() throws Exception{
        // given
        productRepository.save(product);

        Auction auction = Auction.builder()
                .startPrice(1000000)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(5))
                .build();

        auction.assignToProduct(product);
        auctionRepository.save(auction);

        // when
        AuctionResponseDto response = auctionService.placeQuickBid(auction.getId(), bidder);

        // then
        assertThat(response.getCurrentPrice()).isGreaterThan(1000000);
        assertThat(response.getWinnerNickname()).isEqualTo(bidder.getNickname());
    }

    //본인또는 관리자만 경매 취소 가능
    @Test
    public void cancel() throws Exception{
        // given
        productRepository.save(product);

        Auction auction = Auction.builder()
                .startPrice(1000000)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(5))
                .build();

        auction.assignToProduct(product);
        auctionRepository.save(auction);

        // when
        auctionService.cancelAuction(auction.getId(), seller);

        // then
        Auction canceled = auctionRepository.findById(auction.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(AuctionStatus.CANCELED);
    }

    //입찰이 존재할때 삭제불가
    @Test
    public void cancelFail_existsBid() throws Exception {
        // given
        productRepository.save(product);
        Auction auction = Auction.builder()
                .startPrice(1000000)
                .startAt(LocalDateTime.now().minusMinutes(1))
                .endAt(LocalDateTime.now().plusMinutes(5))
                .build();
        auction.assignToProduct(product);
        auctionRepository.save(auction);
        auction.placeBid(bidder, 1100000);
        auctionRepository.save(auction);

        // when & then
        assertThatThrownBy(() -> auctionService.cancelAuction(auction.getId(), seller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CANNOT_CANCEL_WITH_BIDS.getMessage());
    }

}