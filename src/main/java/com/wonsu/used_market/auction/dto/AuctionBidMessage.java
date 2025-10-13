package com.wonsu.used_market.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
//클라이언트와 서버간의 입찰메시지
public class AuctionBidMessage {

    private Long auctionId;
    private String bidder;
    private int bidAmount;
}
