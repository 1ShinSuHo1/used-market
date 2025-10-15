package com.wonsu.used_market.chat.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        log.error("[STOMP ERROR] {}", ex.getMessage(), ex);

        String errorJson = String.format("{\"error\":\"%s\"}", ex.getMessage());
        return MessageBuilder
                .withPayload(errorJson.getBytes(StandardCharsets.UTF_8))
                .setHeader("content-type", "application/json")
                .build();
    }
}
