package com.wonsu.used_market.product.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.QProduct;
import com.wonsu.used_market.product.dto.ProductSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.wonsu.used_market.product.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductSearchCond cond, Pageable pageable) {
        QProduct product = QProduct.product;

        //데이터 조회
        List<Product> content = queryFactory.selectFrom(product)
                .where(
                        keywordContains(cond.getKeyword()),
                        categoryEq(cond.getCategory()),
                        statusEq(cond.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

        // 전체 카운트 조회
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        keywordContains(cond.getKeyword()),
                        categoryEq(cond.getCategory()),
                        statusEq(cond.getStatus())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 동적 조건 메서드(null 자동무시를 위해 설정)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? product.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression categoryEq(String category) {
        return StringUtils.hasText(category) ? product.category.stringValue().eq(category) : null;
    }

    private BooleanExpression statusEq(String status) {
        return StringUtils.hasText(status) ? product.status.stringValue().eq(status) : null;
    }
}
