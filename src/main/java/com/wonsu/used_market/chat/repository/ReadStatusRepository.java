package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.domain.ChatMessage;
import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.ReadStatus;
import com.wonsu.used_market.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long>, ReadStatusRepositoryCustom {

    // 사용자의 채팅방에서 읽지 않은 메시지 조회
    List<ReadStatus> findByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);

    // 사용자가 특정 메시지를 읽었는지
    Optional<ReadStatus> findByChatMessageAndUser(ChatMessage chatMessage, User user);


}