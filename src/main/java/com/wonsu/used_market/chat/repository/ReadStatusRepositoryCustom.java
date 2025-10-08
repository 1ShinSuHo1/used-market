package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.dto.RoomUnreadCountDto;

import java.util.List;

public interface ReadStatusRepositoryCustom {
    //여러방의 안읽은 개수 집계함수
    List<RoomUnreadCountDto> countUnreadByRoomIds(Long userId, List<Long> roomIds);
}
