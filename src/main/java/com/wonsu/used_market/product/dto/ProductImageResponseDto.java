package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponseDto {
    private Long id;
    private String imageUrl;
    private boolean thumbnail;

    public ProductImageResponseDto(ProductImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
        this.thumbnail = image.isThumbnail();
    }
}
