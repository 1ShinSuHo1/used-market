package com.wonsu.used_market.transaction.domain;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

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
    public Transaction(Product product, User buyer, User seller,
                       Integer priceFinal, Long chatRoomId, LocalDateTime completedAt) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
        this.priceFinal = priceFinal;
        this.chatRoomId = chatRoomId;
        this.status = TransactionStatus.PENDING;
        this.completedAt = completedAt != null ? completedAt : LocalDateTime.now();
    }


    //상태 전환을 위한 메서드
    public void confirm() {
        if (this.status != TransactionStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_TRANSACTION_STATE);
        }
        this.status = TransactionStatus.CONFIRMED;
    }

    public void complete() {
        if (this.status != TransactionStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_TRANSACTION_STATE);
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == TransactionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.TRANSACTION_ALREADY_COMPLETED);
        }
        this.status = TransactionStatus.CANCELLED;
    }
}
