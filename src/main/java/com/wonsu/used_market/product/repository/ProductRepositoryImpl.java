package com.wonsu.used_market.product.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.dto.ProductListResponseDto;
import com.wonsu.used_market.product.dto.ProductSearchCond;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.wonsu.used_market.product.domain.QProduct.product;
import static com.wonsu.used_market.product.domain.QProductImage.productImage;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    //상품 상세조회를위한 페치조인
    @Override
    public Product findWithImages(Long productId) {
        return queryFactory.selectFrom(product)
                .leftJoin(product.images, productImage).fetchJoin()
                .where(product.id.eq(productId))
                .fetchOne();
    }

    //상품목록조회 N+1쿼리 문제방지를 위한 쿼리
    @Override
    public Page<ProductListResponseDto> search(ProductSearchCond cond, Pageable pageable) {
        List<ProductListResponseDto> content = queryFactory
                .select(Projections.constructor(ProductListResponseDto.class,
                        product.id,
                        product.title,
                        product.price,
                        product.maker,
                        product.category.stringValue(),
                        product.aiGrade,
                        product.createdAt,
                        productImage.imageUrl
                ))
                .from(product)
                .leftJoin(product.images, productImage)
                .on(productImage.thumbnail.isTrue())
                .where(
                        keywordContains(cond.getKeyword()),
                        categoryEq(cond.getCategory()),
                        statusEq(cond.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();

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
