package com.wonsu.used_market.common.websocket;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        long now = System.currentTimeMillis();

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == null) {
            return message;
        }

        log.info("─────────────── [STOMP preSend {}] cmd={} dest={} session={} user={} ───────────────",
                now, accessor.getCommand(), accessor.getDestination(), accessor.getSessionId(), accessor.getUser());

        try {
            return handle(message, accessor);
        } finally {
            log.info("─────────────── [STOMP preSend END {}] ───────────────", now);
        }
    }

    private Message<?> handle(Message<?> message, StompHeaderAccessor accessor) {
        StompCommand cmd = accessor.getCommand();

        switch (cmd) {
            case CONNECT -> {
                // ✅ Handshake에서 이미 Principal 세팅 끝남
                log.info("[CONNECT] session={} user={}", accessor.getSessionId(), accessor.getUser());
                return message;
            }
            case SUBSCRIBE -> {
                handleSubscribe(accessor);
                return message;
            }
            case SEND -> {
                handleSend(accessor);
                return message;
            }
            case DISCONNECT -> {
                log.info("[DISCONNECT] session={}", accessor.getSessionId());
                return message;
            }
            default -> {
                return message;
            }
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String dest = accessor.getDestination();
        log.info("🟡 SUBSCRIBE dest={}", dest);

        if (dest == null) return;

        if (accessor.getUser() == null) {
            throw new BusinessException(ErrorCode.JWT_INVALID); // 사실상 "인증 안 된 WebSocket"
        }

        // /topic/chat/{roomId} 구독 시 방 참여자 검증
        if (dest.startsWith("/topic/chat/")) {
            String roomIdStr = dest.substring("/topic/chat/".length());
            Long roomId = Long.parseLong(roomIdStr);

            String email = accessor.getUser().getName(); // Authentication.getName() == username(email)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            boolean ok = chatParticipantRepository.findByChatRoomAndUser(room, user).isPresent();
            log.info("[SUBSCRIBE] room={}, user={}, participant={}", roomId, email, ok);

            if (!ok) {
                throw new BusinessException(ErrorCode.NO_PERMISSION);
            }
        }

        // /topic/auction/** 는 자유 구독
        if (dest.startsWith("/topic/auction/")) {
            log.info("[SUBSCRIBE] Auction 채널 → unrestricted");
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        String dest = accessor.getDestination();
        log.info("🟠 SEND dest={}", dest);

        if (dest == null) return;

        if (accessor.getUser() == null) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }

        // /publish/{roomId} 를 통한 채팅 메시지 전송 시 방 참여자 검증
        if (dest.startsWith("/publish/")) {
            String roomIdStr = dest.substring("/publish/".length());
            Long roomId = Long.parseLong(roomIdStr);

            String email = accessor.getUser().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            boolean ok = chatParticipantRepository.findByChatRoomAndUser(room, user).isPresent();
            log.info("[SEND] room={}, user={}, participant={}", roomId, email, ok);

            if (!ok) {
                throw new BusinessException(ErrorCode.NO_PERMISSION);
            }
        }
    }
}
