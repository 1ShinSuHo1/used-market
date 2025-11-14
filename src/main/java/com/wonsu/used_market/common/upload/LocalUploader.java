package com.wonsu.used_market.common.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
@Profile("local")
@Slf4j
public class LocalUploader implements FileUploader {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public String uploadToTemp(MultipartFile file) {
        try {
            String dir = uploadDir + "/temp/";
            File tempDir = new File(dir);
            if (!tempDir.exists()) tempDir.mkdirs();

            String safe = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = System.currentTimeMillis() + "_" + safe;

            File dest = new File(dir + fileName);
            file.transferTo(dest);

            return urlPrefix + "temp/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("TEMP 업로드 실패", e);
        }
    }

    @Override
    public String moveTempToProduct(String tempUrl, Long productId) {
        try {
            String tempPath = tempUrl.replace(urlPrefix, uploadDir); // .../uploads/temp/...
            File tempFile = new File(tempPath);

            String newDir = uploadDir + "/" + productId + "/";
            File pd = new File(newDir);
            if (!pd.exists()) pd.mkdirs();

            File newFile = new File(newDir + tempFile.getName());

            boolean ok = tempFile.renameTo(newFile);
            if (!ok) throw new RuntimeException("TEMP 이동 실패");

            return urlPrefix + productId + "/" + newFile.getName();

        } catch (Exception e) {
            throw new RuntimeException("TEMP 이동 실패", e);
        }
    }

    @Override
    public String uploadToProduct(MultipartFile file, Long productId) {
        try {
            String dir = uploadDir + "/" + productId + "/";
            File pd = new File(dir);
            if (!pd.exists()) pd.mkdirs();

            String safe = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
            String fileName = System.currentTimeMillis() + "_" + safe;

            File dest = new File(dir + fileName);
            file.transferTo(dest);

            return urlPrefix + productId + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("PRODUCT 업로드 실패", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        try {
            String path = imageUrl.replace(urlPrefix, uploadDir);
            File file = new File(path);
            if (file.exists()) file.delete();

        } catch (Exception e) {
            log.error("[LocalUploader] 삭제 실패 {}", imageUrl);
        }
    }
}
