package com.wonsu.used_market.auction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.auction.dto.AuctionBidMessage;
import com.wonsu.used_market.common.websocket.RedisChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionStompService {
    //Redis에 데이터저장용
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    //Stomp를 통해 입찰요청이 들어오면 실행되는데
    //Redis 해시에 현재 경매의 최신상태를 기록하고 pub/sub을 사용하여 모든서버에 보내주기
    public void placeBid(Long auctionId, String bidder, int bidAmount) {
        try {

            //write-behind, Redis의 해시 자료구조를 통해 경매별 상태 를 저장
            String key = "auction:" + auctionId;
            redisTemplate.opsForHash().put(key, "currentPrice", bidAmount);
            redisTemplate.opsForHash().put(key, "winner", bidder);

            //pub/sub단계 메시지 Json변환해서 채널에 발행
            AuctionBidMessage message = new AuctionBidMessage(auctionId, bidder, bidAmount);
            String json = objectMapper.writeValueAsString(message);

            // 트랜잭션 커밋후에 구독하고 있는 모든서버에 메시지전달
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.convertAndSend(RedisChannels.AUCTION, json);
                        log.info("[AUCTION PUB - AFTER COMMIT] {}", json);
                    }
                });
            } else {
                // 트랜잭션이 없는 상황(비동기 호출/테스트 등)
                redisTemplate.convertAndSend(RedisChannels.AUCTION, json);
                log.info("[AUCTION PUB - NO TX] {}", json);
            }

        } catch (Exception e) {
            log.error("placeBid failed", e);
        }
    }
}
