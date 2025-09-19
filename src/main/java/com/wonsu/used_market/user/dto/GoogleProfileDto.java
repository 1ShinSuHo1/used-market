package com.wonsu.used_market.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)//없는 필드는 자동무시
public class GoogleProfileDto {

    private String sub;
    private String email;
    private boolean email_verified;
}
