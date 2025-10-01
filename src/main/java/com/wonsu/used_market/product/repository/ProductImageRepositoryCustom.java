package com.wonsu.used_market.product.repository;

import com.wonsu.used_market.product.dto.ProductImageResponseDto;

import java.util.List;

public interface ProductImageRepositoryCustom {
    List<ProductImageResponseDto> findDtosByProductId(Long productId);
}
