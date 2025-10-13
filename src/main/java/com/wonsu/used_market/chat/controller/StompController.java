package com.wonsu.used_market.chat.controller;

import com.wonsu.used_market.chat.dto.ChatMessageRequestDto;
import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import com.wonsu.used_market.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/{roomId}") //클라이언트에서 특정 형태로 메시지발행시 메시지매핑이 수신
    // DestinationVariable은 메시지매핑 어노테이션으로 정의된 웹소켓 컨트롤러 내에서만 사용
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageRequestDto payload, Principal principal) {

        if (principal == null) {
            throw new com.wonsu.used_market.exception.BusinessException(
                    com.wonsu.used_market.exception.ErrorCode.JWT_INVALID
            );
        }

        // 스푸핑방지를 위해 클라이언트가 보낸 이메일은 무시하고, 보안을위해서 서버가 신뢰하는 이메일로 대체
        payload = new ChatMessageRequestDto(
                payload.getMessage(),
                principal.getName(),
                payload.getType(),
                payload.getFileUrl()
        );
        ChatMessageResponseDto res = chatMessageService.saveAndSendMessage(roomId, payload);
        log.info("sent -> roomId={}, sender={}, type={}, at={}",
                res.getRoomId(), res.getSenderNickname(), res.getType(), res.getCreatedAt());
    }


}
