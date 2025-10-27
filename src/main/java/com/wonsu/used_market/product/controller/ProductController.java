package com.wonsu.used_market.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.product.dto.*;
import com.wonsu.used_market.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    @Value("${app.upload.dir}")
    private String appUploadDir;

    @Value("${app.upload.url-prefix}")
    private String appUrlPrefix;

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    //상품 등록
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CreateProductResponseDto> createProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestPart(value = "req") @Valid String reqJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        // json 문자열을 dto로 반환
        CreateProductRequestDto req = objectMapper.readValue(reqJson, CreateProductRequestDto.class);

        //환경설정값 주입
        // 환경설정값 주입
        String uploadDir = appUploadDir.endsWith("/") ? appUploadDir : appUploadDir + "/";
        String urlPrefix = appUrlPrefix.endsWith("/") ? appUrlPrefix : appUrlPrefix + "/";

        // 파일이 존재한다?? 그러면 로컬경로로 업로드후 url 세팅
        if (file != null && !file.isEmpty()) {
            String originalName = (file.getOriginalFilename() != null)
                    ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "unknown";
            String fileName = System.currentTimeMillis() + "_" + originalName;

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            String finalUrl = urlPrefix + fileName;
            log.info("[파일 업로드 성공] {}", finalUrl);

            req.addImage(finalUrl, true);
        }
        CreateProductResponseDto response = productService.createProduct(principal.getUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
