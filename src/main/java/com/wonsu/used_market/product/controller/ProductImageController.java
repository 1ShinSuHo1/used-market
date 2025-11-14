package com.wonsu.used_market.product.controller;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.upload.FileUploader;

import com.wonsu.used_market.product.dto.ProductImageRequestDto;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ProductImageService productImageService;
    private final FileUploader fileUploader;



    @PostMapping
    public ResponseEntity<?> uploadImages(
            @PathVariable Long productId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "thumbnailIndex", required = false) Integer thumbnailIndex
    ) {

        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile f = files.get(i);
                if (!f.isEmpty()) {
                    String url = fileUploader.uploadToProduct(f, productId);
                    boolean isThumb = (thumbnailIndex != null && thumbnailIndex == i);

                    productImageService.addImage(productId, new ProductImageRequestDto(url, isThumb));
                    log.info("[상품 이미지 추가 업로드] productId={}, url={}, thumbnail={}", productId, url, isThumb);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
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
