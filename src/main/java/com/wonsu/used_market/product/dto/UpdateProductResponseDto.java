package com.wonsu.used_market.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductResponseDto {
    private Long id;
    private LocalDateTime updatedAt;
}
