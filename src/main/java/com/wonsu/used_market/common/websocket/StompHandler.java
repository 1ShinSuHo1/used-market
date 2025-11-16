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
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // handshake등 명령없는 메시지 무시
        if (accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("STOMP CONNECT 요청 - 토큰 검증 시작");

            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                // handshake 중일 수 있으니 그냥 통과시킴
                log.warn("STOMP CONNECT 요청 - Authorization 헤더 없음 (handshake 단계일 수 있음)");
                return message;
            }

            String token = bearerToken.substring(7);

            // JWT 유효성 검증
            jwtTokenProvider.validateToken(token);

            // 이메일 추출
            String email = jwtTokenProvider.getEmail(token);

            // DB 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // CustomUserDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(user);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

            // STOMP 세션에 Principal 저장
            accessor.setUser(authentication);

            log.info("STOMP CONNECT 토큰 검증 성공: user={}", email);
            return message;
        }

        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            String dest = accessor.getDestination();
            if (dest == null) return message;

            // 채팅 구독: /topic/chat/{roomId}
            if (dest.startsWith("/topic/chat/")) {

                if (accessor.getUser() == null) {
                    throw new BusinessException(ErrorCode.JWT_INVALID);
                }

                String roomIdStr = dest.substring("/topic/chat/".length());
                Long roomId = Long.parseLong(roomIdStr);

                String email = accessor.getUser().getName();
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                ChatRoom room = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

                boolean participant = chatParticipantRepository.findByChatRoomAndUser(room, user).isPresent();
                if (!participant) {
                    throw new BusinessException(ErrorCode.NO_PERMISSION);
                }

                return message;
            }

            // 경매 구독: /topic/auction/{auctionId}
            if (dest.startsWith("/topic/auction/")) {
                // 경매 구독은 인증만 되어 있으면 모두 허용 (비공개 채널이 아님)
                return message;
            }
        }



        return message;
    }

}
