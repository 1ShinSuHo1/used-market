package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.domain.ChatParticipant;
import com.wonsu.used_market.user.domain.User;

import java.util.List;

public interface ChatParticipantRepositoryCustom {
    List<ChatParticipant> findAllWithRoomAndProductByUser(User user);
}
