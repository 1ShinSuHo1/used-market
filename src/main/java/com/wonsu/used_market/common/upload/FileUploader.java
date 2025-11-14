package com.wonsu.used_market.common.upload;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {
    String uploadToTemp(MultipartFile file);                      // 상품 등록 단계 TEMP 업로드
    String moveTempToProduct(String tempUrl, Long productId);     // TEMP → products/{productId}/
    String uploadToProduct(MultipartFile file, Long productId);   // 기존 상품에 이미지 추가
    void delete(String imageUrl);                 // 삭제
}
