package com.wonsu.used_market.product.repository;

import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.dto.ProductListResponseDto;
import com.wonsu.used_market.product.dto.ProductSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface ProductRepositoryCustom {
    Page<ProductListResponseDto> search(ProductSearchCond cond, Pageable pageable);
    Product findWithImages(Long productId);
}
