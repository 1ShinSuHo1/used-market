package com.wonsu.used_market.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 요청 값입니다."),
    // 회원관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),

    // 파일 업로드 관련 예외
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다."),

    // AI관련
    AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AI 서버와의 통신 중 오류가 발생했습니다."),
    AI_PREDICTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 예측 결과를 처리할 수 없습니다."),


    // 상품관련
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 카테고리 값입니다."),
    INVALID_SALETYPE(HttpStatus.BAD_REQUEST, "잘못된 판매 방식 값입니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, "해당 권한이 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    THUMBNAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "썸네일 이미지를 찾을 수 없습니다."),
    DUPLICATE_THUMBNAIL(HttpStatus.BAD_REQUEST, "상품에는 하나의 썸네일만 지정할 수 있습니다."),

    //거래 관련
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다."),
    TRANSACTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 상품에 대한 거래가 존재합니다."),
    INVALID_TRANSACTION_STATE(HttpStatus.BAD_REQUEST, "잘못된 거래 상태입니다."),
    TRANSACTION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 거래입니다."),
    TRANSACTION_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 거래입니다."),

    // 경매관련
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."),
    AUCTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "상품에 이미 연결된 경매가 존재합니다."),
    AUCTION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "진행 중인 경매가 아닙니다."),
    AUCTION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "경매 시간이 유효하지 않습니다."),
    INVALID_AUCTION_TIME(HttpStatus.BAD_REQUEST, "경매 시작/종료 시간이 올바르지 않습니다."),
    INVALID_BID_AMOUNT(HttpStatus.BAD_REQUEST, "입찰 금액이 유효하지 않습니다."),
    INVALID_BID_UNIT(HttpStatus.BAD_REQUEST, "입찰 금액 단위가 올바르지 않습니다."),
    BID_TOO_LOW(HttpStatus.BAD_REQUEST,  "입찰 금액이 너무 낮습니다."),
    CANNOT_CANCEL_WITH_BIDS(HttpStatus.BAD_REQUEST, "입찰자가 있어 경매를 취소할 수 없습니다."),// 경매 관련

    // 채팅 관련
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),
    CHAT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채팅방에 접근할 수 없습니다."),

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
