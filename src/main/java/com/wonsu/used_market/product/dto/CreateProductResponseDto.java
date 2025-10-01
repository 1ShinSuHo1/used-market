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

    public CreateProductResponseDto(Product product) {
        this.id = product.getId();
        this.sellerId = product.getSeller().getId();
        this.status = product.getStatus().name();
        this.images = product.getImages().stream()
                .map(ProductImageResponseDto::new)
                .collect(Collectors.toList());
    }
}
