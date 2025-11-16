package com.wonsu.used_market.common.websocket;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.auth.JwtTokenProvider;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        long now = System.currentTimeMillis();

        try {
            log.info("─────────────── [STOMP preSend START {}] ───────────────", now);

            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            if (accessor.getCommand() == null) {
                log.info("[NO COMMAND] message bypass");
                return message;
            }

            log.info("[COMMAND] {}", accessor.getCommand());
            log.info("[HEADERS] {}", accessor.toNativeHeaderMap());
            log.info("[DESTINATION] {}", accessor.getDestination());
            log.info("[SESSION ID] {}", accessor.getSessionId());
            log.info("[USER] {}", accessor.getUser());

            return handle(message, accessor);

        } catch (Exception ex) {
            log.error("🔥🔥 [STOMP ERROR at {}] – Full Stacktrace:", now, ex);
            throw ex;
        } finally {
            log.info("─────────────── [STOMP preSend END   {}] ───────────────", now);
        }
    }

    private Message<?> handle(Message<?> message, StompHeaderAccessor accessor) {

        StompCommand cmd = accessor.getCommand();

        switch (cmd) {

            case CONNECT:
                return onConnect(message, accessor);

            case SUBSCRIBE:
                return onSubscribe(message, accessor);

            case SEND:
                log.info("[SEND] 메시지 전송 요청");
                return message;

            case DISCONNECT:
                log.info("[DISCONNECT] 클라이언트가 연결 종료 요청");
                return message;
        }

        return message;
    }

    private Message<?> onConnect(Message<?> message, StompHeaderAccessor accessor) {

        log.info("🔵 CONNECT 핸들러 진입");

        String bearer = accessor.getFirstNativeHeader("Authorization");
        log.info("[CONNECT] Authorization 헤더 = {}", bearer);

        if (bearer == null || !bearer.startsWith("Bearer ")) {
            log.warn("[CONNECT] Authorization 없음 → handshake 단계일 가능성 → 통과");
            return message;
        }

        String token = bearer.substring(7);
        log.info("[CONNECT] 추출된 JWT = {}", token);

        jwtTokenProvider.validateToken(token);

        String email = jwtTokenProvider.getEmail(token);
        log.info("[CONNECT] JWT 이메일 = {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth =
                new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

        accessor.setUser(auth);

        log.info("🟢 CONNECT 완료: user={}, session={}", email, accessor.getSessionId());
        return message;
    }

    private Message<?> onSubscribe(Message<?> message, StompHeaderAccessor accessor) {

        String dest = accessor.getDestination();
        log.info("🟡 SUBSCRIBE 요청 dest = {}", dest);

        if (dest == null) return message;

        // 채팅 구독
        if (dest.startsWith("/topic/chat/")) {

            log.info("[SUBSCRIBE] Chat Subscribe 로직 실행");

            if (accessor.getUser() == null) {
                log.error("[SUBSCRIBE] User null → JWT 상실");
                throw new BusinessException(ErrorCode.JWT_INVALID);
            }

            String roomIdStr = dest.substring("/topic/chat/".length());
            log.info("[SUBSCRIBE] Parsed roomId = {}", roomIdStr);

            Long roomId = Long.parseLong(roomIdStr);

            String email = accessor.getUser().getName();
            log.info("[SUBSCRIBE] email from Principal = {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            ChatRoom room = chatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            boolean ok = chatParticipantRepository.findByChatRoomAndUser(room, user).isPresent();
            log.info("[SUBSCRIBE] room participant = {}", ok);

            if (!ok) throw new BusinessException(ErrorCode.NO_PERMISSION);

            log.info("🟢 SUBSCRIBE 완료: user={}, room={}", email, roomId);
            return message;
        }

        // 경매 구독
        if (dest.startsWith("/topic/auction/")) {
            log.info("[SUBSCRIBE] Auction 채널 → unrestricted");
            return message;
        }

        log.info("[SUBSCRIBE] 기타 구독 → 통과");
        return message;
    }
}
