package com.wonsu.used_market.chat.controller;

import com.wonsu.used_market.chat.dto.ChatMessageRequestDto;
import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import com.wonsu.used_market.chat.service.ChatMessageService;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/{roomId}") //클라이언트에서 특정 형태로 메시지발행시 메시지매핑이 수신
    // DestinationVariable은 메시지매핑 어노테이션으로 정의된 웹소켓 컨트롤러 내에서만 사용
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageRequestDto payload, Principal principal) {

        if (principal == null) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }

        String email = principal.getName();

        log.info("[STOMP CONTROLLER] , roomId={}, sender={}, payload={}",
                 roomId, email, payload);

        ChatMessageResponseDto res = chatMessageService.saveAndSendMessage(roomId, email,  payload);
        log.info("sent -> roomId={}, sender={}, type={}, at={}",
                res.getRoomId(), res.getSenderNickname(), res.getType(), res.getCreatedAt());
    }


}
