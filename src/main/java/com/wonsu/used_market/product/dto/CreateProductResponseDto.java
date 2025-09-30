package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CreateProductResponseDto {
    private Long id;
    private Long sellerId;
    private String status;

    public CreateProductResponseDto(Product product) {
        this.id = product.getId();
        this.sellerId = product.getSeller().getId();
        this.status = product.getStatus().name();
    }
}
