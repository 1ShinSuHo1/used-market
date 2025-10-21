package com.wonsu.used_market.auction.controller;

import com.wonsu.used_market.auction.dto.AuctionBidMessage;
import com.wonsu.used_market.auction.service.AuctionStompService;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuctionStompController {

    private final AuctionStompService auctionStompService;


    @MessageMapping("/auction/{auctionId}/bid")
    public void handleBid(@DestinationVariable Long auctionId, AuctionBidMessage message, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);

        // 스푸핑 방지 – 서버에서 인증된 사용자 이름으로 교체ㄴ
        String bidder = principal.getName();
        auctionStompService.placeBid(auctionId, bidder, message.getBidAmount());

        log.info("[STOMP BID] auctionId={} bidder={} bid={}", auctionId, bidder, message.getBidAmount());
    }
}
