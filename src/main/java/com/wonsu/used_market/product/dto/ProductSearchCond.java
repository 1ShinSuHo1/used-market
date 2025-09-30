package com.wonsu.used_market.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductSearchCond {
    private String keyword;
    private String category;
    private String status;
}
