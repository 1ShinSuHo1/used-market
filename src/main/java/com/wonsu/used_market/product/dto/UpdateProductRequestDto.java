package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.ProductStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequestDto {

    @Size(max = 100, message = "상품 제목은 최대 100자까지 입력 가능합니다.")
    private String title;

    @Size(max = 200, message = "상품 설명은 최대 200자까지 입력 가능합니다.")
    private String description;

    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private Integer price;
    private ProductStatus status;
}
