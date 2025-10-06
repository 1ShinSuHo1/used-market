package com.wonsu.used_market.auction.dto;

import com.wonsu.used_market.auction.domain.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuctionResponseDto {
    private Long auctionId;
    private Long productId;
    private String productTitle;
    private int currentPrice;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private String winnerNickname;

    public static AuctionResponseDto from(Auction auction) {
        return new AuctionResponseDto(
                auction.getId(),
                auction.getProduct().getId(),
                auction.getProduct().getTitle(),
                auction.getCurrentPrice(),
                auction.getStartAt(),
                auction.getEndAt(),
                auction.getStatus().name(),
                auction.getWinner() != null ? auction.getWinner().getNickname() : null
        );
    }
}
