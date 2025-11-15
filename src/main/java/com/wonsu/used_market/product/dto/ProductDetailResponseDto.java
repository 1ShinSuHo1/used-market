package com.wonsu.used_market.product.dto;

import com.wonsu.used_market.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponseDto {

    //상품 기본 정보
    private Long id;
    private String title;
    private String description;
    private Integer price;
    private String status;
    private LocalDateTime createdAt;

    private String category;
    private String saleType;

    //상품 세부속성
    private String maker;
    private String modelSeries;
    private String modelVariant;
    private Integer storageGb;
    private String usagePeriod;
    private String aiGrade;
    private String wishLocation;

    //판매자와 이미지 정보
    private SellerInfo seller;
    private List<ImageInfo> images;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerInfo {
        private Long id;
        private String nickname;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageInfo {
        private Long id;
        private String url;
        private boolean thumbnail;
    }

    // 엔티티 → DTO 변환 생성자
    public ProductDetailResponseDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.status = product.getStatus().name();
        this.createdAt = product.getCreatedAt();
        this.saleType = product.getSaleType().name();

        this.category = product.getCategory().name();

        this.maker = product.getMaker();
        this.modelSeries = product.getModelSeries();
        this.modelVariant = product.getModelVariant();
        this.storageGb = product.getStorageGb();
        this.usagePeriod = product.getUsagePeriod();
        this.aiGrade = product.getAiGrade();
        this.wishLocation = product.getWishLocation();

        this.seller = new SellerInfo(
                product.getSeller().getId(),
                product.getSeller().getNickname()
        );

        this.images = product.getImages().stream()
                .map(img -> new ImageInfo(
                        img.getId(),
                        img.getImageUrl(),
                        img.isThumbnail()
                ))
                .collect(Collectors.toList());
    }


}
