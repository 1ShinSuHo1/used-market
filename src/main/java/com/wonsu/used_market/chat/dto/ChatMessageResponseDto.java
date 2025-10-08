package com.wonsu.used_market.chat.dto;

import com.wonsu.used_market.chat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {

    private Long roomId;
    private String senderNickname;
    private String message;
    private MessageType type;
    private LocalDateTime createdAt;
}
