package com.wonsu.used_market.auction.repository;

import com.wonsu.used_market.auction.domain.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom{


    //경매시스템 동시 입찰방지를 위해서 비관적 락 사용(경쟁상태 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.id = :auctionId")
    Optional<Auction> findByIdForUpdate(@Param("auctionId") Long auctionId);


    //이미 종료된 경매를 찾아내는 메서드
    @Query("select a from Auction a where a.endAt < :now and a.status = 'ACTIVE'")
    List<Auction> findExpiredAuctions(@Param("now") LocalDateTime now);
}
