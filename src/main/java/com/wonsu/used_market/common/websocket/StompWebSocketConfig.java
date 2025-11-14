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

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public StompWebSocketConfig(StompHandler stompHandler, StompExceptionHandler stompExceptionHandler) {
        this.stompHandler = stompHandler;
        this.stompExceptionHandler = stompExceptionHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // /publish/1형태로 메시지 발행해야함
        // /publish로 시작하는 url 패턴으로 메시자가 발행되면 @Controller 객체가 @MessaMapping메서드로 라우팅된다
        registry.setApplicationDestinationPrefixes("/publish");

        // /topic/1형태로 메시지를 수신
        registry.enableSimpleBroker("/topic");


    }

    // 웹소켓 요청시에 인터셉터를 통해 토큰 검증하기
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(stompHandler);
    }
    
    //에러 잡아주기
    @Bean
    public StompExceptionHandler stompErrorHandler() {
        return stompExceptionHandler;
    }



    //스톰프 페이로드를 json으로 변환하도록 강제
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
