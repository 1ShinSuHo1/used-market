package com.wonsu.used_market.product.controller;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.product.dto.*;
import com.wonsu.used_market.product.service.ProductService;
import com.wonsu.used_market.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품 등록
    @PostMapping
    public ResponseEntity<CreateProductResponseDto> createProduct(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateProductRequestDto req) {
        CreateProductResponseDto response = productService.createProduct(principal.getUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //상품 목록 조회
    @GetMapping
    public ResponseEntity<Page<ProductListResponseDto>> getProducts(ProductSearchCond cond, Pageable pageable){
        Page<ProductListResponseDto> products = productService.getProducts(cond, pageable);
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
