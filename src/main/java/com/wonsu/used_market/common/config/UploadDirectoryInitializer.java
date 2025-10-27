package com.wonsu.used_market.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
// 업로드 디렉토리 초기화를 위한 컴포넌트
public class UploadDirectoryInitializer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        File dir = new File(uploadDir);

        //디렉토리가 존재하지않을경우 생성
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info(" [UploadDirectoryInitializer] Upload directory created successfully: {}", dir.getAbsolutePath());
            } else {
                log.warn(" [UploadDirectoryInitializer] Failed to create upload directory: {}", dir.getAbsolutePath());
            }
        } else {
            log.info(" [UploadDirectoryInitializer] Upload directory already exists: {}", dir.getAbsolutePath());
        }
    }

}
