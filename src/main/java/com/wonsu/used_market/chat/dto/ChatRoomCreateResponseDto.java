package com.wonsu.used_market.chat.dto;

import com.wonsu.used_market.chat.domain.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateResponseDto {

    private Long roomId;
    private ChatRoomType roomType;
    private String roomName;
    private boolean isClosed;


}
