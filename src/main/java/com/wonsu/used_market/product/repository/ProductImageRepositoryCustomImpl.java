package com.wonsu.used_market.product.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.dto.QProductImageResponseDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.wonsu.used_market.product.domain.QProductImage.productImage;

@RequiredArgsConstructor
public class ProductImageRepositoryCustomImpl implements ProductImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductImageResponseDto> findDtosByProductId(Long productId) {
        return queryFactory
                .select(new QProductImageResponseDto(
                        productImage.id,
                        productImage.imageUrl,
                        productImage.thumbnail
                ))
                .from(productImage)
                .where(productImage.product.id.eq(productId))
                .fetch();
    }
}
