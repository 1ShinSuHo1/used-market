package com.wonsu.used_market.chat.service;

import com.wonsu.used_market.chat.domain.ChatParticipant;
import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;


    //채팅방에 유저를 추가
    @Transactional
    public void addParticipant(ChatRoom chatRoom, User user) {
        boolean exists = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).isPresent();

        // 이미 존재하지 않을 때만 추가
        if (!exists) {
            ChatParticipant participant = ChatParticipant.create(chatRoom, user);
            chatParticipantRepository.save(participant);
        }
    }
}
