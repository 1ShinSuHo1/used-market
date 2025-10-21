package com.wonsu.used_market.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {


    @NotBlank(message = "카테고리를 입력해주세요.")
    private String category;

    @NotBlank(message = "제조사를 입력해주세요.")
    private String maker;

    private String modelSeries;
    private String modelVariant;
    private Integer storageGb;

    @NotBlank(message = "상품 제목을 입력해주세요.")
    private String title;

    @Size(max = 2000, message = "상품 설명은 최대 2000자까지 입력 가능합니다.")
    private String description;

    private String usagePeriod;

    @NotBlank(message = "판매 방식을 입력해주세요.")
    private String saleType; // DIRECT / AUCTION

    @NotNull(message = "가격을 입력해주세요.")
    private Integer price;

    private String wishLocation;


    @Valid
    @NotNull(message = "상품 이미지는 최소 1개 이상 등록해야 합니다.")
    private List<ProductImageRequestDto> images = new ArrayList<>();

    public void addImage(String imageUrl, boolean thumbnail) {
        this.images.add(new ProductImageRequestDto(imageUrl, thumbnail));
    }

}
