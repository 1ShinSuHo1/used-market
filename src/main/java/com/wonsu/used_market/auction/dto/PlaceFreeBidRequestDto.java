package com.wonsu.used_market.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceFreeBidRequestDto {
    private Long auctionId;
    private int bidAmount;
}