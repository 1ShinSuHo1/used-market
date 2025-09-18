package com.wonsu.used_market.exception;

public class GoogleAuthException extends BusinessException {
    public GoogleAuthException() {
        super(ErrorCode.GOOGLE_AUTH_FAILED);
    }

    public GoogleAuthException(Throwable cause) {
        super(ErrorCode.GOOGLE_AUTH_FAILED, cause);
    }

}
