package com.wonsu.used_market.chat.config;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("STOMP CONNECT 요청 - 토큰 검증 시작");

            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new BusinessException(ErrorCode.JWT_INVALID);
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
        }

        return message;
    }

}
