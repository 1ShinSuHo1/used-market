package com.wonsu.used_market.product.controller;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.product.dto.ProductImageRequestDto;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    //상품 이미지 등록
    @PostMapping
    public ResponseEntity<ProductImageResponseDto> addImage(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequestDto req
    ) {
        ProductImageResponseDto response = productImageService.addImage(productId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //상품 이미지 전체 조회
    @GetMapping
    public ResponseEntity<List<ProductImageResponseDto>> getImages(
            @PathVariable Long productId
    ) {
        List<ProductImageResponseDto> images = productImageService.getImages(productId);
        return ResponseEntity.ok(images);
    }

    //썸네일 사진 조회
    @GetMapping("/thumbnail")
    public ResponseEntity<ProductImageResponseDto> getThumbnail(
            @PathVariable Long productId
    ) {
        ProductImageResponseDto thumbnail = productImageService.getThumbnail(productId);
        return ResponseEntity.ok(thumbnail);
    }

    //썸네일 사진 바꾸기
    @PatchMapping("/{imageId}/thumbnail")
    public ResponseEntity<ProductImageResponseDto> updateThumbnail(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        ProductImageResponseDto response = productImageService.updateThumbnail(imageId, principal.getUser());
        return ResponseEntity.ok(response);
    }

    //상품이미지 삭제인데 권한체크
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        productImageService.deleteImage(imageId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
