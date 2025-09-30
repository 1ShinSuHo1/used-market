package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequestDto {

    private String title;
    private String description;
    private Integer price;
    private ProductStatus status;
}
