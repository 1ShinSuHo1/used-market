package com.wonsu.used_market.transaction.domain;

public enum TransactionStatus {
    PENDING, // 경매 종료 후 자동 생성(판매자가 수락 대기)
    CONFIRMED, // 판매자가 수락
    COMPLETED, // 거래 완료
    CANCELLED,
}
