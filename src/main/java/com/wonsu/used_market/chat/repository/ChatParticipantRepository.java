package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.domain.ChatParticipant;
import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> , ChatParticipantRepositoryCustom {

    // 채팅방의 모든 참여자
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    // 사용자가 채팅방에 참여 중인지 확인
    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    //N+1방지
    @Query("select cp from ChatParticipant cp join fetch cp.user where cp.chatRoom = :room")
    List<ChatParticipant> findByChatRoomFetchUser(@Param("room") ChatRoom room);
}
