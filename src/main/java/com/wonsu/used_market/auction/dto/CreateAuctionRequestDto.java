package com.wonsu.used_market.auction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuctionRequestDto {

    private Long productId;
    private int startPrice;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
