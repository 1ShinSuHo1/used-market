package com.wonsu.used_market.common.upload;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class S3Uploader implements FileUploader {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    @Value("${cloud.aws.s3.prefix.temp}")
    private String tempPrefix;

    @Value("${cloud.aws.s3.prefix.product}")
    private String productPrefix;

    @PostConstruct
    void normalize() {
        // baseUrl은 항상 "/"로 끝나게 통일
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        // prefix들도 안전하게 "/"로 끝나게 맞춰두기
        if (!tempPrefix.endsWith("/")) {
            tempPrefix = tempPrefix + "/";
        }
        if (!productPrefix.endsWith("/")) {
            productPrefix = productPrefix + "/";
        }

        log.info("[S3Uploader] baseUrl={}, tempPrefix={}, productPrefix={}", baseUrl, tempPrefix, productPrefix);
    }


    //상품 등록 단계 TEMP 업로드
    @Override
    public String uploadToTemp(MultipartFile file) {
        try {
            String safe = file.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String fileName = Instant.now().toEpochMilli() + "_" + safe;

            // key: products/temp/파일명
            String key = tempPrefix + fileName;

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String url = baseUrl + key;
            log.info("[S3Uploader] TEMP 업로드 성공 key={} url={}", key, url);
            return url;

        } catch (IOException e) {
            log.error("[S3Uploader] TEMP 업로드 실패", e);
            throw new RuntimeException("TEMP 업로드 실패", e);
        }
    }


    // TEMP → products/{productId}/ 이동
    @Override
    public String moveTempToProduct(String tempUrl, Long productId) {
        try {
            // tempUrl 에서 S3 key만 뽑기 (baseUrl 기준)
            String key = extractKeyFromUrl(tempUrl);          // 예: products/temp/xxx.jpg
            String fileName = key.substring(key.lastIndexOf("/") + 1);

            // 새 key: products/{productId}/파일명
            String newKey = productPrefix + productId + "/" + fileName;

            s3.copyObject(
                    CopyObjectRequest.builder()
                            .sourceBucket(bucket)
                            .sourceKey(key)
                            .destinationBucket(bucket)
                            .destinationKey(newKey)
                            .build()
            );

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            );

            String newUrl = baseUrl + newKey;
            log.info("[S3Uploader] TEMP → PRODUCT 이동 성공 {} -> {}", tempUrl, newUrl);
            return newUrl;

        } catch (Exception e) {
            log.error("[S3Uploader] TEMP → PRODUCT 이동 실패 url={}", tempUrl, e);
            throw new RuntimeException("TEMP 이동 실패", e);
        }
    }


    //  기존 상품에 이미지 바로 추가
    @Override
    public String uploadToProduct(MultipartFile file, Long productId) {
        try {
            String safe = file.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String fileName = Instant.now().toEpochMilli() + "_" + safe;

            // key: products/{productId}/파일명
            String key = productPrefix + productId + "/" + fileName;

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String url = baseUrl + key;
            log.info("[S3Uploader] PRODUCT 업로드 성공 key={} url={}", key, url);
            return url;

        } catch (IOException e) {
            log.error("[S3Uploader] PRODUCT 업로드 실패", e);
            throw new RuntimeException("PRODUCT 업로드 실패", e);
        }
    }


    //파일 삭제
    @Override
    public void delete(String imageUrl) {
        try {
            String key = extractKeyFromUrl(imageUrl); // baseUrl 이후부터 key로 사용

            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());

            log.info("[S3Uploader] 삭제 성공 url={} key={}", imageUrl, key);
        } catch (Exception e) {
            log.error("[S3Uploader] 삭제 실패 url={}", imageUrl, e);
        }
    }


    // S3 URL → key 변환
    private String extractKeyFromUrl(String url) {

        if (url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length());
        }

        // 혹시 다른 형식으로 들어온 경우 대비 (fallback)
        int idx = url.indexOf(".amazonaws.com/");
        if (idx != -1) {
            return url.substring(idx + ".amazonaws.com/".length());
        }

        // 그래도 못 찾으면 그냥 전체를 key로 사용
        return url;
    }
}
