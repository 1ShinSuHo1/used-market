package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
public class CreateProductResponseDto {
    private Long id;
    private Long sellerId;
    private String status;
    private List<ProductImageResponseDto> images;
    private String aiGrade;
    private Double confidence;

    public CreateProductResponseDto(Product product,Double confidence) {
        this.id = product.getId();
        this.sellerId = product.getSeller().getId();
        this.status = product.getStatus().name();
        this.aiGrade = product.getAiGrade();
        this.confidence = confidence;
        this.images = product.getImages().stream()
                .map(ProductImageResponseDto::new)
                .collect(Collectors.toList());
    }
}
