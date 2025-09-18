package com.wonsu.used_market.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 에러 모음
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),
    GOOGLE_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "구글 인증 실패"),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "카카오 인증 실패");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
