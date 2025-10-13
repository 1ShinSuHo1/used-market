package com.wonsu.used_market.common.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//스프링과 스톰프는 기본적으로 세션관리를 내부적으로 처리해줌
//연결/해제 이벤트 기록 연결된 세션수를 실시간으로 확인할목적
@Component
@Slf4j
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        sessions.add(sessionId);
        log.info("[SESSION] CONNECT: sessionId={} | activeSessions={}", sessionId, sessions.size());
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        sessions.remove(sessionId);
        log.info("[SESSION] DISCONNECT: sessionId={} | activeSessions={}", sessionId, sessions.size());
    }
}
