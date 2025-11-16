package com.wonsu.used_market.common.websocket;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketJwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        log.info("🔵 WebSocket Handshake 시작: uri={}", request.getURI());

        String token = null;

        // 1) Header 에서 Authorization 꺼보기
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String bearer = httpRequest.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                token = bearer.substring(7);
            }
        }

        // 2) 없으면 query string에서 token=... 꺼내기
        if (token == null) {
            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
            String qp = queryParams.getFirst("token");
            if (qp != null && !qp.isBlank()) {
                token = qp;
            }
        }

        if (token == null) {
            log.warn("❌ WebSocket Handshake: 토큰 없음 → 연결 거부");
            return false; // 인증 없는 웹소켓은 거부
        }

        try {
            // JWT 검증
            jwtTokenProvider.validateToken(token);
            String email = jwtTokenProvider.getEmail(token);
            log.info("✅ WebSocket Handshake JWT OK, email={}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            CustomUserDetails userDetails = new CustomUserDetails(user);

            // 나중에 HandshakeHandler에서 Principal로 꺼내 쓸 수 있게 attributes에 저장
            attributes.put("userDetails", userDetails);

            return true;
        } catch (BusinessException ex) {
            log.error("❌ WebSocket Handshake 실패: {}", ex.getErrorCode().getMessage());
            return false;
        } catch (Exception ex) {
            log.error("❌ WebSocket Handshake 예외", ex);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 필요 없으면 비워둠
    }
}
