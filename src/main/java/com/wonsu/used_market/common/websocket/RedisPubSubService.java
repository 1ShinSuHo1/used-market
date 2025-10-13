package com.wonsu.used_market.common.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonsu.used_market.auction.dto.AuctionBidMessage;
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

    // 레디스가 수신하여 스톰프로 전송(chat채널과 auction채널의 종류에따라 달라진다)
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 어떤 채널인지 확인해야함
            String channel = new String(pattern);
            String payload = new String(message.getBody());

            // 챗 채널 메시지 처리
            if (channel.contains(RedisChannels.CHAT)) {
                ChatMessageResponseDto dto = objectMapper.readValue(payload, ChatMessageResponseDto.class);

                // 구독자에게 전송 (/topic/{roomId})
                messaging.convertAndSend("/topic/" + dto.getRoomId(), dto);

                log.debug("[CHAT PUBSUB] roomId={}, sender={}", dto.getRoomId(), dto.getSenderNickname());
            }

            // 옥션 채널 메시지 처리
            else if (channel.contains(RedisChannels.AUCTION)) {
                AuctionBidMessage msg = objectMapper.readValue(payload, AuctionBidMessage.class);

                // 구독자에게 전송 (/topic/auction/{auctionId})
                messaging.convertAndSend("/topic/auction/" + msg.getAuctionId(), msg);

                log.debug("[AUCTION PUBSUB] auctionId={}, bidder={}, bid={}",
                        msg.getAuctionId(), msg.getBidder(), msg.getBidAmount());

            }

        } catch (Exception e) {
            log.error("Redis onMessage handling failed", e);
        }
    }


}
