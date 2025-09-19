package com.wonsu.used_market.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직에서 발생하는 예외처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("BusinessException 발생: {}", errorCode.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getMessage()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    // 예상치못한 오류 일때 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected exception 발생: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse(
                500,
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.status(500).body(response);
    }


}
