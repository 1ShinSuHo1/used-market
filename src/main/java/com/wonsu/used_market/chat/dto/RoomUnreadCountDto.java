package com.wonsu.used_market.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomUnreadCountDto {
    private Long roomId;
    private Long cnt;
}
