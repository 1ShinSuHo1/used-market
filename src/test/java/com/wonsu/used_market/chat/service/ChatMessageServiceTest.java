package com.wonsu.used_market.chat.service;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.MessageType;
import com.wonsu.used_market.chat.dto.ChatMessageRequestDto;
import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import com.wonsu.used_market.chat.repository.ChatMessageRepository;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.chat.repository.ReadStatusRepository;
import com.wonsu.used_market.common.websocket.RedisPubSubService;
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChatMessageServiceTest {

    //레디스 발행 목으로 대체
    @TestConfiguration
    static class MockConfig {
        @Bean
        RedisPubSubService redisPubSubService() {
            return Mockito.mock(RedisPubSubService.class);
        }
    }

    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired private ChatRoomService chatRoomService;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatParticipantRepository chatParticipantRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ReadStatusRepository readStatusRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private RedisPubSubService redisPubSubService;

    private User seller;
    private User buyer;
    private Product product;
    private ChatRoom room;

    @BeforeEach
    void init() {
        Mockito.reset(redisPubSubService);
        seller = userRepository.save(User.builder()
                .email("seller@test.com")
                .nickname("판매자")
                .password("1234")
                .build());

        buyer = userRepository.save(User.builder()
                .email("buyer@test.com")
                .nickname("구매자")
                .password("1234")
                .build());

        product = productRepository.save(Product.builder()
                .seller(seller)
                .category(Category.NOTEBOOK)
                .maker("Apple")
                .title("테스트 맥북")
                .saleType(SaleType.DIRECT)
                .price(2000000)
                .description("테스트용 제품 설명")
                .aiGrade("A")
                .modelSeries("MacBook Pro")
                .modelVariant("M2 13inch")
                .storageGb(512)
                .usagePeriod("1년 미만")
                .wishLocation("서울 강남구")
                .build());

        room = chatRoomRepository.findById(
                chatRoomService.createDirectRoom(product.getId(), buyer.getId()).getRoomId()
        ).orElseThrow();
    }

    // 메시지 저장및 발행 검증 테스트
    @Test
    public void messageSaved() throws Exception{
        // given
        ChatMessageRequestDto req = new ChatMessageRequestDto(
                "안녕하세요 테스트 메시지입니다.",
                seller.getEmail(),
                MessageType.TEXT,
                null
        );
        // when
        ChatMessageResponseDto res = chatMessageService.saveAndSendMessage(room.getId(), seller.getEmail(), req);

        // then
        assertThat(res.getRoomId()).isEqualTo(room.getId());
        assertThat(res.getSenderNickname()).isEqualTo(seller.getNickname());
        assertThat(res.getMessage()).isEqualTo("안녕하세요 테스트 메시지입니다.");
        assertThat(res.getType()).isEqualTo(MessageType.TEXT);

        // Redis 발행 여부 검증
        Mockito.verify(redisPubSubService, Mockito.times(1))
                .publish(Mockito.anyString());

        // DB 반영 검증
        assertThat(chatMessageRepository.count()).isEqualTo(1);
        assertThat(readStatusRepository.count()).isEqualTo(2);
    }

    //안읽은 메시지를 모두 읽음처리하는 테스트
    @Test
    public void markAllAsRead() throws Exception{
        // given
        ChatMessageRequestDto req = new ChatMessageRequestDto(
                "읽음테스트",
                seller.getEmail(),
                MessageType.TEXT,
                null
        );
        chatMessageService.saveAndSendMessage(room.getId(), seller.getEmail(), req);
        // when
        chatMessageService.markAllAsRead(room.getId(), buyer.getId());

        // then
        long unread = readStatusRepository.findByChatRoomAndUserAndIsReadFalse(room, buyer).size();
        assertThat(unread).isZero();
    }

    //이전 대화기록을 조회하는 테스트
    @Test
    public void getChatHistory() throws Exception{
        // given
        chatMessageService.saveAndSendMessage(
                room.getId(),
                seller.getEmail(),
                new ChatMessageRequestDto("첫번째", seller.getEmail(), MessageType.TEXT, null)
        );

        chatMessageService.saveAndSendMessage(
                room.getId(),
                buyer.getEmail(),
                new ChatMessageRequestDto("두번째", buyer.getEmail(), MessageType.TEXT, null)
        );

        // when
        List<ChatMessageResponseDto> history = chatMessageService.getChatHistory(room.getId());

        // then
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getMessage()).isEqualTo("첫번째");
        assertThat(history.get(1).getMessage()).isEqualTo("두번째");
    }

}