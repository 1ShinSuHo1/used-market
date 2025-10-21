package com.wonsu.used_market.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.chat.constant.RedisChannels;
import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPubSubService implements MessageListener {



    private final StringRedisTemplate stringRedisTemplate; // 레디스발행
    private final SimpMessageSendingOperations messaging; // 스톰프
    private final ObjectMapper objectMapper;

    //JSON 문자열을 chat 채널로발행
    public void publish(String messageJson){
        stringRedisTemplate.convertAndSend(RedisChannels.CHAT, messageJson);
    }

    //

    // 레디스가 수신하여 스톰프로 전송
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            ChatMessageResponseDto dto = objectMapper.readValue(payload, ChatMessageResponseDto.class);

            // 구독자에게 전송 (/topic/{roomId})
            messaging.convertAndSend("/topic/" + dto.getRoomId(), dto);

            log.debug("[Redis→STOMP] roomId={}, sender={}, type={}",
                    dto.getRoomId(), dto.getSenderNickname(), dto.getType());
        } catch (Exception e) {
            log.error("Redis onMessage handling failed", e);
        }
    }


}
