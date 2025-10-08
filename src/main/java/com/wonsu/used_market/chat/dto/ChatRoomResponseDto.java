package com.wonsu.used_market.chat.dto;

import com.wonsu.used_market.chat.domain.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {

    private Long roomId;
    private String roomName;
    private ChatRoomType roomType;
    private boolean isClosed;
    private String productTitle;
    private String sellerNickname;
    private Long unReadCount;
}
