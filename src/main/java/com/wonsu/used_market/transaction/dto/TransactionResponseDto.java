package com.wonsu.used_market.transaction.dto;

import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private Long productId;
    private String productTitle;
    private String buyerNickname;
    private String sellerNickname;
    private Integer priceFinal;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Builder
    public TransactionResponseDto(Long id,
                                  Long productId,
                                  String productTitle,
                                  String buyerNickname,
                                  String sellerNickname,
                                  Integer priceFinal,
                                  TransactionStatus status,
                                  LocalDateTime createdAt,
                                  LocalDateTime completedAt) {
        this.id = id;
        this.productId = productId;
        this.productTitle = productTitle;
        this.buyerNickname = buyerNickname;
        this.sellerNickname = sellerNickname;
        this.priceFinal = priceFinal;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static TransactionResponseDto from(Transaction tx) {
        return TransactionResponseDto.builder()
                .id(tx.getId())
                .productId(tx.getProduct().getId())
                .productTitle(tx.getProduct().getTitle())
                .buyerNickname(tx.getBuyer().getNickname())
                .sellerNickname(tx.getSeller().getNickname())
                .priceFinal(tx.getPriceFinal())
                .status(tx.getStatus())
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .build();
    }



}
