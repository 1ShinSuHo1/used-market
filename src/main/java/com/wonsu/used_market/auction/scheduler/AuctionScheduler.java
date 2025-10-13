package com.wonsu.used_market.auction.scheduler;

import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    //1분마다 경매 종료 여부를 체크하여 자동으로 상태를 변경
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> expired = auctionRepository.findExpiredAuctions(LocalDateTime.now());
        for (Auction a : expired) {
            try {
                a.endAuction();
                auctionRepository.save(a);
                redisTemplate.delete("auction:" + a.getId());
                log.info("[SCHEDULER] Auction auto-ended: id={}", a.getId());
            } catch (Exception e) {
                log.error("[SCHEDULER] Failed to close auction: id={}", a.getId(), e);
            }
        }
    }
}
