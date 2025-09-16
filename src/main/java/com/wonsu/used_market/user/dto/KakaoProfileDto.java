package com.wonsu.used_market.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)//없는 필드는 자동무시
public class KakaoProfileDto {

    private String id;
    private KakaoAccount kakao_account;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)//없는 필드는 자동무시
    public static class KakaoAccount {
        private String email;
        private String is_email_verified;
    }
}
