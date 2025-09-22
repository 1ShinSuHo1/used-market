package com.wonsu.used_market.user.service;

import com.wonsu.used_market.exception.KakaoAuthException;
import com.wonsu.used_market.user.dto.AccessTokenDto;
import com.wonsu.used_market.user.dto.KakaoProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;


@Service
@Transactional(readOnly = true)
@Slf4j
public class KakaoService {
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    public AccessTokenDto getAccessToken(String code) {

        try {
            RestClient restClient = RestClient.create();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", kakaoClientId);
            params.add("client_secret", kakaoClientSecret);
            params.add("redirect_uri", kakaoRedirectUri);
            params.add("grant_type", "authorization_code");

            ResponseEntity<AccessTokenDto> response = restClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .header("Content-Type", "application/x-www-form-urlencoded;")
                    .body(params)
                    .retrieve()
                    .toEntity(AccessTokenDto.class);

            log.info("응답 accesstoken Json: {}", response.getBody());
            return response.getBody();
        }catch (Exception e) {
            log.error("카카오 AccessToken 발급 실패: {}", e.getMessage(),e);
            throw new KakaoAuthException(e);
        }
    }


    public KakaoProfileDto getKakaoProfile(String token){
        try {
            RestClient restClient = RestClient.create();

            ResponseEntity<KakaoProfileDto> response = restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toEntity(KakaoProfileDto.class);

            return response.getBody();
        }catch (Exception e) {
            log.error("카카오 프로필 조회 실패: {}", e.getMessage(),e);
            throw new KakaoAuthException(e);
        }
    }
}
