package com.wonsu.used_market.chat.dto;

import com.wonsu.used_market.chat.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    private String message;
    private String senderEmail;
    private MessageType type;
    private String fileUrl;
}
