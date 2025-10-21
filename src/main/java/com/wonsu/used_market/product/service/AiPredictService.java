package com.wonsu.used_market.product.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class AiPredictService {

    @Value("${ai.base-url}")
    private String baseUrl;


    private final WebClient webClient;

    public AiPredictService(@Value("${ai.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    //AI 서버에 이미지를 전송하고 등급과 confidence를 받아옴
    public AiResponse predict(String imageUrl) {
        log.info("[AI 예측 요청 시작] 요청 이미지: {}", imageUrl);
        try {
            AiResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(Map.of("image_url", imageUrl))
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block(); // 비동기 -> 동기처리

            if (response == null || response.getGrade() == null) {
                log.warn("[AI 예측 결과 없음] FastAPI 응답이 비어있거나 유효하지 않습니다. imageUrl={}", imageUrl);
                throw new BusinessException(ErrorCode.AI_PREDICTION_FAILED);
            }

            log.info("[AI 예측 성공] grade={}, confidence={}%", response.getGrade(), response.getConfidence());
            return response;

        } catch (WebClientResponseException e) {
            log.error("[AI 서버 응답 오류] status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[AI 서버 통신 실패] message={} imageUrl={}", e.getMessage(), imageUrl, e);
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }



    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString
    public static class AiResponse {
        private String grade;       // 예측 등급 (A/B/C)
        private Double confidence;  // 신뢰도 (퍼센트)

        public static AiResponse of(String grade, Double confidence) {
            return new AiResponse(grade, confidence);
        }

        public static AiResponse fromApi(String grade, Double confidence) {
            if (grade == null || grade.isBlank()) {
                throw new IllegalArgumentException("AI 응답 grade가 비어있습니다.");
            }
            return new AiResponse(grade, confidence);
        }
    }
}
