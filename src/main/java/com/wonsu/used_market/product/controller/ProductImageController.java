package com.wonsu.used_market.product.controller;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.dto.ProductImageRequestDto;
import com.wonsu.used_market.product.dto.ProductImageResponseDto;
import com.wonsu.used_market.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping
    public ResponseEntity<ProductImageResponseDto> uploadOrAddImage(
            @PathVariable Long productId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "thumbnail", defaultValue = "false") boolean thumbnail
    ) {
        try {
            String finalUrl;

            // 파일이 있으면 로컬에저장
            if (file != null && !file.isEmpty()) {
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String originalName = (file.getOriginalFilename() != null)
                        ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                        : "unknown";

                String fileName = System.currentTimeMillis() + "_" + originalName;
                File dest = new File(uploadDir + fileName);
                file.transferTo(dest);

                finalUrl = urlPrefix + fileName;
            }
            // 외부 url이 있다 그러면 그대로 사용
            else if (imageUrl != null && !imageUrl.isBlank()) {
                finalUrl = imageUrl;
            }
            // 아무것도 없으면 예외
            else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            // 기존 서비스 로직 재사용 (DB 등록)
            ProductImageRequestDto req = new ProductImageRequestDto(finalUrl, thumbnail);
            ProductImageResponseDto response = productImageService.addImage(productId, req);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }
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
