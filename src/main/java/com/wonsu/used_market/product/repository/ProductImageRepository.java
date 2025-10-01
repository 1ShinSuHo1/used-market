package com.wonsu.used_market.product.repository;

import com.wonsu.used_market.product.domain.ProductImage;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> ,ProductImageRepositoryCustom{
    //특정 상품에 속한 모든 이미지 조회
    List<ProductImage> findByProductId(Long productId);

    //특정 상품의 썸네일 조회
    ProductImage findByProductIdAndThumbnailTrue(Long productId);

    //썸네일 중복방지
    boolean existsByProductIdAndThumbnailTrue(Long productId);


}
