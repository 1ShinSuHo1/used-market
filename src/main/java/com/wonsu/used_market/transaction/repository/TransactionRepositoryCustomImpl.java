package com.wonsu.used_market.transaction.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.product.domain.QProduct;
import com.wonsu.used_market.transaction.domain.QTransaction;
import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.user.domain.QUser;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // Q도메인 객체 생성 (QueryDSL 자동 생성 클래스)
    private final QTransaction tx = QTransaction.transaction;
    private final QProduct product = QProduct.product;
    private final QUser buyer = new QUser("buyer");
    private final QUser seller = new QUser("seller");

    @Override
    public List<Transaction> findAllByUserWithFetch(User user) {
        return queryFactory
                .selectFrom(tx)
                .join(tx.product, product).fetchJoin()
                .join(tx.buyer, buyer).fetchJoin()
                .join(tx.seller, seller).fetchJoin()
                .where(tx.buyer.eq(user).or(tx.seller.eq(user)))
                .orderBy(tx.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<Transaction> findAllByUserWithFetch(User user, Pageable pageable) {
        // 데이터 조회
        List<Transaction> results = queryFactory
                .selectFrom(tx)
                .join(tx.product, product).fetchJoin()
                .join(tx.buyer, buyer).fetchJoin()
                .join(tx.seller, seller).fetchJoin()
                .where(tx.buyer.eq(user).or(tx.seller.eq(user)))
                .orderBy(tx.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 조회
        Long count = queryFactory
                .select(tx.count())
                .from(tx)
                .where(tx.buyer.eq(user).or(tx.seller.eq(user)))
                .fetchOne();

        // Page 객체로 반환
        return PageableExecutionUtils.getPage(results, pageable, () -> count);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(tx)
                .where(tx.product.id.eq(productId))
                .fetchFirst();

        return fetchOne != null;
    }
}
