package com.wonsu.used_market.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@Slf4j
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;

    public StompController(SimpMessageSendingOperations messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @MessageMapping("/{roomId}") //클라이언트에서 특정 형태로 메시지발행시 메시지매핑이 수신
    // DestinationVariable은 메시지매핑 어노테이션으로 정의된 웹소켓 컨트롤러 내에서만 사용
    public void sendMessage(@DestinationVariable Long roomId, String message, Principal principal) {
        String email = principal.getName();
        log.info("roomId={}, sender={}, message={}", roomId, email, message);

    }
}
