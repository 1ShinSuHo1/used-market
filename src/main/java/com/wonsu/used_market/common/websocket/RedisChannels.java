package com.wonsu.used_market.common.websocket;

// chat을 매직스트링으로 직접 사용하지않기 위해 클래스 만들기
public class RedisChannels {

    private  RedisChannels() {
    }
    // 채팅 메시지 송수신용 채널
    public static final String CHAT = "chat";

    // 경매 입찰 송수신용 채널
    public static final String AUCTION = "auction";
}
