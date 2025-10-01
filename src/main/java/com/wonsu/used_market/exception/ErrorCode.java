package com.wonsu.used_market.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 회원관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),

    // 상품관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 카테고리 값입니다."),
    INVALID_SALETYPE(HttpStatus.BAD_REQUEST, "잘못된 판매 방식 값입니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, "해당 상품에 대한 수정 권한이 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    THUMBNAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "썸네일 이미지를 찾을 수 없습니다."),

    // OAuth 관련
    GOOGLE_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "구글 인증 실패"),
    KAKAO_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "카카오 인증 실패"),

    // JWT 관련
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    JWT_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 JWT 토큰입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
