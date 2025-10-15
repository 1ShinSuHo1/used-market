package com.wonsu.used_market.auction.scheduler;

import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.auction.repository.AuctionRepository;
import com.wonsu.used_market.transaction.service.TransactionService;
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
    private final TransactionService transactionService;

    //1분마다 경매 종료 여부를 체크하여 자동으로 상태를 변경
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> expired = auctionRepository.findExpiredAuctions(LocalDateTime.now());

        for (Auction a : expired) {
            try {
                //경매 종료하기
                a.endAuction();
                auctionRepository.save(a);

                //낙찰자 존재시 거래 자동생성
                if (a.getWinner() != null) {
                    transactionService.createTransaction(
                            a.getProduct(),                // 상품
                            a.getWinner(),                 // 낙찰자
                            a.getProduct().getSeller(),    // 판매자
                            a.getCurrentPrice()            // 최종 낙찰가
                    );
                    log.info("[AUTO TRANSACTION CREATED] auctionId={}, winner={}, price={}",
                            a.getId(), a.getWinner().getNickname(), a.getCurrentPrice());
                } else {
                    log.info("[NO WINNER] Auction ended without bids: auctionId={}", a.getId());
                }

                //레디스 캐시제거
                redisTemplate.delete("auction:" + a.getId());
                log.info("[SCHEDULER] Auction auto-ended: id={}", a.getId());
            } catch (Exception e) {
                log.error("[SCHEDULER] Failed to close auction: id={}", a.getId(), e);
            }
        }
    }
}
