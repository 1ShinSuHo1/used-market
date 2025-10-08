package com.wonsu.used_market.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.chat.domain.*;
import com.wonsu.used_market.chat.dto.ChatMessageRequestDto;
import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import com.wonsu.used_market.chat.repository.ChatMessageRepository;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.chat.repository.ReadStatusRepository;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final RedisPubSubService redisPubSubService;
    private final ObjectMapper objectMapper;

    //메시지 저장 및 Redis 발행
    @Transactional
    public ChatMessageResponseDto saveAndSendMessage(Long roomId, ChatMessageRequestDto req) {
        // 채팅방/보낸유저 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        User sender = userRepository.findByEmail(req.getSenderEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 해당 방의 참여자인지 권한 체크해주기
        boolean participant = chatParticipantRepository.findByChatRoomAndUser(room, sender).isPresent();
        if (!participant) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }


        // 메시지 저장
        MessageType type = (req.getType() != null) ? req.getType() : MessageType.TEXT;

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .type(type)
                .content(req.getMessage())
                .fileUrl(req.getFileUrl())
                .build();

        chatMessageRepository.save(chatMessage);

        // 참여자별로 읽은 상태를 생성 (보낸 본인은 당연히 true)
        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoomFetchUser(room);
        for (ChatParticipant p : participants) {
            ReadStatus rs = ReadStatus.builder()
                    .chatRoom(room)
                    .chatMessage(chatMessage)
                    .user(p.getUser())
                    .isRead(p.getUser().equals(sender))
                    .build();
            readStatusRepository.save(rs);
        }

        // 응답 객체 만들어주기
        ChatMessageResponseDto res = new ChatMessageResponseDto(
                room.getId(),
                sender.getNickname(),
                chatMessage.getContent(),
                chatMessage.getType(),
                chatMessage.getCreatedAt()
        );

        // Redis Pub/Sub 발행하기
        try {
            String json = objectMapper.writeValueAsString(res);
            redisPubSubService.publish(json);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return res;
    }

    // 특정 방의 이전 메시지
    public List<ChatMessageResponseDto> getChatHistory(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room)
                .stream()
                .map(m -> new ChatMessageResponseDto(
                        room.getId(),
                        m.getSender().getNickname(),
                        m.getContent(),
                        m.getType(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    // 방 기준으로 특정 유저의 안읽은 메시지 모두 읽음처리
    @Transactional
    public void markAllAsRead(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<ReadStatus> unread = readStatusRepository.findByChatRoomAndUserAndIsReadFalse(room, user);
        unread.forEach(ReadStatus::markAsRead);
    }
}
