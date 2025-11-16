package com.wonsu.used_market.common.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final StompExceptionHandler stompExceptionHandler;
    private final WebSocketJwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public StompWebSocketConfig(StompHandler stompHandler,
                                StompExceptionHandler stompExceptionHandler,
                                WebSocketJwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.stompHandler = stompHandler;
        this.stompExceptionHandler = stompExceptionHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                // ✅ Handshake 단계에서 JWT 검증 + Principal 세팅
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new WebSocketPrincipalHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /publish/** 로 들어온 메시지는 @MessageMapping 으로 라우팅
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic/chat/** , /topic/auction/** 는 브로커에서 구독 처리
        registry.enableSimpleBroker("/topic/chat", "/topic/auction");
    }

    // STOMP inbound 채널 인터셉터 – 여기서는 JWT 말고 방 권한 검증만
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }

    // 에러 핸들러 빈 등록
    @Bean
    public StompExceptionHandler stompErrorHandler() {
        return stompExceptionHandler;
    }

    // STOMP 페이로드를 JSON으로 강제
    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        MappingJackson2MessageConverter jackson = new MappingJackson2MessageConverter();
        jackson.setContentTypeResolver(resolver);
        converters.add(jackson);

        return false; // 기존 컨버터도 유지
    }
}
