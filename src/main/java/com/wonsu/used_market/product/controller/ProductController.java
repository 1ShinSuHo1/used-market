package com.wonsu.used_market.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.upload.FileUploader;
import com.wonsu.used_market.product.dto.*;
import com.wonsu.used_market.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {


    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final FileUploader fileUploader;

    // 상품 등록 (다중 이미지 + TEMP 업로드)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CreateProductResponseDto> createProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestPart("req") @Valid String reqJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {

        CreateProductRequestDto req = objectMapper.readValue(reqJson, CreateProductRequestDto.class);

        // 1) TEMP 업로드
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile f = files.get(i);
                if (!f.isEmpty()) {
                    String tempUrl = fileUploader.uploadToTemp(f); // TEMP 경로 업로드
                    boolean isThumbnail = (i == 0);                // 첫 번째 이미지를 기본 썸네일로
                    req.addImage(tempUrl, isThumbnail);
                    log.info("[상품 등록 TEMP 업로드] url={}, thumbnail={}", tempUrl, isThumbnail);
                }
            }
        }

        // 2) 서비스 계층에서:
        //    - AI 예측
        //    - Product + ProductImage 저장
        //    - TEMP → products/{id}/ 로 이동 + URL 업데이트
        CreateProductResponseDto res = productService.createProduct(principal.getUser(), req);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }


    //상품 목록 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(ProductSearchCond cond, Pageable pageable) {
        Map<String, Object> products = productService.getProducts(cond, pageable);
        return ResponseEntity.ok(products);
    }

    //상품 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponseDto> getProductDetail(
            @PathVariable Long id
    ) {
        ProductDetailResponseDto response = productService.getProductDetail(id);
        return ResponseEntity.ok(response);
    }

    //상품 수정
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateProductResponseDto> updateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProductRequestDto req
    ) {
        UpdateProductResponseDto response = productService.updateProduct(id, req, principal.getUser());
        return ResponseEntity.ok(response);
    }

    //상품삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        productService.deleteProduct(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
