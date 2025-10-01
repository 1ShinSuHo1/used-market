package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.ProductImage;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequestDto {
    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;
    private boolean thumbnail;



}
