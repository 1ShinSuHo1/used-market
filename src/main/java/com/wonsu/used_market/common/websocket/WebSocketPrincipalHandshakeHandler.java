package com.wonsu.used_market.common.websocket;

import com.wonsu.used_market.common.auth.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
public class WebSocketPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Object attr = attributes.get("userDetails");

        if (attr instanceof CustomUserDetails userDetails) {
            // Authentication을 Principal로 쓰자 (Spring Security랑도 잘 맞음)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            log.info("✅ WebSocket Principal 설정 완료: username={}", userDetails.getUsername());
            return auth;
        }

        // 혹시 없으면 기본 동작
        Principal p = super.determineUser(request, wsHandler, attributes);
        log.warn("⚠ WebSocket Principal가 userDetails 없이 생성됨: {}", p);
        return p;
    }
}
