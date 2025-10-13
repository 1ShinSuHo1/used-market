package com.wonsu.used_market.auction.domain;

import com.wonsu.used_market.bid.domain.Bid;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //상품과의 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "start_price", nullable = false)
    private Integer startPrice;

    @Column(name = "current_price", nullable = false)
    private Integer currentPrice;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    //낙찰자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private AuctionStatus status;

    //입찰내역
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    //연관관계 메서드
    public void assignToProduct(Product product) {
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        this.product = product;
        product.assignAuction(this);
    }

    public void addBid(Bid bid) {
        this.bids.add(bid);
    }

    //자유입찰 사용자가 직접 입력
    public void placeBid(User bidder, int bidAmount) {
        if (status != AuctionStatus.ACTIVE)
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        if (bidAmount <= currentPrice)
            throw new BusinessException(ErrorCode.BID_TOO_LOW);

        this.currentPrice = bidAmount;
        this.winner = bidder;

        Bid bid = Bid.builder()
                .bidAmount(bidAmount)
                .bidder(bidder)
                .auction(this)
                .build();

        this.bids.add(bid);
    }


    //경매 종료
    public void endAuction() {
        this.status = AuctionStatus.ENDED;
    }

    //판매자 취소
    public void cancelAuction() {
        if (this.status != AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        this.status = AuctionStatus.CANCELED;
    }

    //운영자 보류
    public void suspendAuction() {
        this.status = AuctionStatus.SUSPENDED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    private Auction(Integer startPrice,
                    LocalDateTime startAt,
                    LocalDateTime endAt) {
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = AuctionStatus.ACTIVE;
    }

}
