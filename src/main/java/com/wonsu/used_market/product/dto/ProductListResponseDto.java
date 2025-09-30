package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
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

        // 썸네일 이미지 (없으면 null)
        this.thumbnailUrl = product.getImages().stream()
                .filter(img -> img.isThumbnail())   // 썸네일로 지정된 이미지 찾기
                .findFirst()
                .map(img -> img.getImageUrl())      // 있으면 URL 꺼내기
                .orElse(null);                      // 없으면 null
    }


}
