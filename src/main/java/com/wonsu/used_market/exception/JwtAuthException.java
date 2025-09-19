package com.wonsu.used_market.exception;

public class JwtAuthException extends BusinessException {
    public JwtAuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public JwtAuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
