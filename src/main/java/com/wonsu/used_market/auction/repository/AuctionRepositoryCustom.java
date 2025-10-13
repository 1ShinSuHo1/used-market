package com.wonsu.used_market.auction.repository;

import com.wonsu.used_market.auction.domain.Auction;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuctionRepositoryCustom {

    List<Auction> findAllWithAssociations(Pageable pageable);

    long countAll();

}
