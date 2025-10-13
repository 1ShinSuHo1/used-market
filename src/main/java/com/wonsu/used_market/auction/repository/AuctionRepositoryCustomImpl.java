package com.wonsu.used_market.auction.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.auction.domain.Auction;
import com.wonsu.used_market.auction.domain.QAuction;
import com.wonsu.used_market.product.domain.QProduct;
import com.wonsu.used_market.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AuctionRepositoryCustomImpl implements AuctionRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<Auction> findAllWithAssociations(Pageable pageable) {
        QAuction auction = QAuction.auction;
        QProduct product = QProduct.product;
        QUser seller = new QUser("seller");
        QUser winner = new QUser("winner");

        return queryFactory
                .selectFrom(auction)
                .distinct()
                .join(auction.product, product).fetchJoin()
                .join(product.seller, seller).fetchJoin()
                .leftJoin(auction.winner, winner).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(auction.createdAt.desc())
                .fetch();
    }

    @Override
    public long countAll() {
        QAuction auction = QAuction.auction;
        return queryFactory.select(auction.count())
                .from(auction)
                .fetchOne();
    }
}
