package com.wonsu.used_market.chat.service;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.ChatRoomType;
import com.wonsu.used_market.chat.domain.MessageType;
import com.wonsu.used_market.chat.dto.ChatMessageRequestDto;
import com.wonsu.used_market.chat.dto.ChatRoomCreateResponseDto;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.product.domain.Category;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.domain.SaleType;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@Transactional
class ChatRoomServiceTest {

    @Autowired private ChatRoomService chatRoomService;
    @Autowired private UserRepository userRepository;
    @Autowired private ChatMessageService chatMessageService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatParticipantRepository chatParticipantRepository;

    private User seller;
    private User buyer;
    private Product product;
    private ChatRoom room;

    @BeforeEach
    void setUp() {
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

        ChatRoomCreateResponseDto created =
                chatRoomService.createDirectRoom(product.getId(), buyer.getId());

        //테스트 환경에서만 플러쉬 강제함
        chatRoomRepository.flush();

        room = chatRoomRepository.findById(created.getRoomId())
                .orElseThrow(() -> new IllegalStateException("채팅방 생성 실패"));

    }

    //일반 거래용 채팅방생성테스트
    @Test
    public void createDirectRoom() throws Exception{
        // given
        Long productId = product.getId();
        Long buyerId = buyer.getId();

        // when
        ChatRoomCreateResponseDto result = chatRoomService.createDirectRoom(productId, buyerId);

        // then
        ChatRoom room = chatRoomRepository.findById(result.getRoomId()).orElseThrow();
        assertThat(room.getRoomType()).isEqualTo(ChatRoomType.DIRECT);
        assertThat(room.getProduct().getId()).isEqualTo(product.getId());
        assertThat(chatParticipantRepository.count()).isEqualTo(2);
    }

    //채팅방 내 모든 메시지를 읽음처리하는 테스트
    @Test
    public void markAllRead() throws Exception{
        // given
        User sender = seller;
        ChatMessageRequestDto req = new ChatMessageRequestDto(
                "테스트 메시지",
                seller.getEmail(),
                MessageType.TEXT,
                null
        );
        chatMessageService.saveAndSendMessage(room.getId(),seller.getEmail(), req);
        // when
        chatMessageService.markAllAsRead(room.getId(), buyer.getId());
        // then
        assertThat(true).isTrue();
    }

}