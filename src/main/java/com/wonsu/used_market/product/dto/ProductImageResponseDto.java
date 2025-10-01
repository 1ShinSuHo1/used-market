package com.wonsu.used_market.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.wonsu.used_market.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductImageResponseDto {
    private Long id;
    private String imageUrl;
    private boolean thumbnail;

    //엔티티변환용
    public ProductImageResponseDto(ProductImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
        this.thumbnail = image.isThumbnail();
    }

    @QueryProjection
    public ProductImageResponseDto(Long id, String imageUrl, boolean thumbnail) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
    }


}
