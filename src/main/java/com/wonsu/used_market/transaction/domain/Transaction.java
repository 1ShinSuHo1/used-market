package com.wonsu.used_market.transaction.domain;

import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //거래 상품
    @OneToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "product_id",nullable = false,unique = true)
    private Product product;

    //구매자
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "buyer_id",nullable = false)
    private User buyer;

    //판매자
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "seller_id",nullable = false)
    private User seller;

    @Column(name = "price_final",nullable = false)
    private Integer priceFinal;

    // 거래 완료 시점
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.completedAt == null) {
            this.completedAt = this.createdAt;
        }
    }

    // 연관관계 메서드
    public void assignToProduct(Product product) {
        this.product = product;
        product.assignTransaction(this);
    }


    @Builder
    public Transaction(Product product, User buyer, User seller, Integer priceFinal, LocalDateTime completedAt) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
        this.priceFinal = priceFinal;
        this.completedAt = completedAt != null ? completedAt : LocalDateTime.now();
    }
}
