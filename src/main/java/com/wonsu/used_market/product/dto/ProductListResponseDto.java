package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ProductListResponseDto {
    private Long id;
    private String title;
    private Integer price;
    private String maker;
    private String category;
    private String aiGrade;
    private LocalDateTime createdAt;
    private String thumbnailUrl;

    // 엔티티를 DTO로 변환하기
    public ProductListResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.maker = product.getMaker();
        this.category = product.getCategory().name();
        this.aiGrade = product.getAiGrade();
        this.createdAt = product.getCreatedAt();
        this.thumbnailUrl = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    // Projection 전용 생성자
    public ProductListResponseDto(Long id, String title, Integer price, String maker,
                                  String category, String aiGrade, LocalDateTime createdAt,
                                  String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.maker = maker;
        this.category = category;
        this.aiGrade = aiGrade;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }


}
