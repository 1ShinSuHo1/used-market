package com.wonsu.used_market.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true) //없는 필드 자동무시
public class AccessTokenDto {

    private String access_token;
    private int expires_in;
    private String scope;
}
