package com.wonsu.used_market.user.service;

import com.wonsu.used_market.user.dto.AccessTokenDto;
import com.wonsu.used_market.user.dto.GoogleProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    public AccessTokenDto getAccessToken(String code) {
        // 인가코드, clientId, client_secret, redirect_uri, grant_type

        //RestClient 사용 , RestTemplate는 스프링에서 비추천하기때문에
        RestClient restClient = RestClient.create();

        //MultiValurMap을 통해 자동으로 form-data형식으로 body 조립
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded;")
                .body(params)
                // 응답바디값만을 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        System.out.println("응답 accesstoken Json " + response.getBody());
        return response.getBody();

    }


    public GoogleProfileDto getGoogleProfile(String token){
        RestClient restClient = RestClient.create();

        ResponseEntity<GoogleProfileDto> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " +  token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        return response.getBody();

    }
}
