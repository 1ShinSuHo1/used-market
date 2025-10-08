package com.wonsu.used_market.chat.service;


import com.wonsu.used_market.chat.domain.ChatParticipant;
import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.ChatRoomType;
import com.wonsu.used_market.chat.dto.ChatRoomCreateResponseDto;
import com.wonsu.used_market.chat.dto.ChatRoomResponseDto;
import com.wonsu.used_market.chat.repository.ChatParticipantRepository;
import com.wonsu.used_market.chat.repository.ChatRoomRepository;
import com.wonsu.used_market.chat.repository.ReadStatusRepository;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//채팅방 새성, 참가자 관리, 방 조회 서비스 다루는 서비스코드
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final UserRepository userRepository;
    private final ChatParticipantService chatParticipantService;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final ReadStatusRepository readStatusRepository;

    // 일반 거래용 채팅방 생성
    @Transactional
    public ChatRoomCreateResponseDto createDirectRoom(Long productId,Long buyerId){
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));


        //같은 채팅방이 있는지 확인 페치조인으로 N+1문제 막아두었음
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByProductAndRoomType(product, ChatRoomType.DIRECT);

        // 존재하면 그 방을 그대로 반환
        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            return new ChatRoomCreateResponseDto(
                    room.getId(), room.getRoomType(), room.getName(), room.isClosed()
            );
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .roomType(ChatRoomType.DIRECT)
                .isClosed(false)
                .build();

        chatRoomRepository.save(chatRoom);

        //방에 판매자와 구매자 추가
        chatParticipantService.addParticipant(chatRoom, product.getSeller());
        chatParticipantService.addParticipant(chatRoom, buyer);

        return new ChatRoomCreateResponseDto(
                chatRoom.getId(),
                chatRoom.getRoomType(),
                chatRoom.getName(),
                chatRoom.isClosed()
        );
    }

    //경매 시작전 문의 채팅방 생성
    @Transactional
    public ChatRoomCreateResponseDto createAuctionInquiryRoom(Long productId,Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 기존에 방이 있는지 확인하고 있으면 재사용
        Optional<ChatRoom> existingRoom =
                chatRoomRepository.findByProductAndRoomType(product, ChatRoomType.AUCTION_INQUIRY);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            return new ChatRoomCreateResponseDto(
                    room.getId(), room.getRoomType(), room.getName(), room.isClosed()
            );
        }


        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .roomType(ChatRoomType.AUCTION_INQUIRY)
                .isClosed(false)
                .build();

        chatRoomRepository.save(chatRoom);

        // 사용자들 등록
        chatParticipantService.addParticipant(chatRoom, product.getSeller());
        chatParticipantService.addParticipant(chatRoom, user);

        return new ChatRoomCreateResponseDto(
                chatRoom.getId(),
                chatRoom.getRoomType(),
                chatRoom.getName(),
                chatRoom.isClosed()
        );
    }

    //거래 완료후에 자동 채팅방 생성
    @Transactional
    public ChatRoomCreateResponseDto createAfterTradeRoom(Transaction transaction){
        Product product = transaction.getProduct();

        // 기존 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom =
                chatRoomRepository.findByProductAndRoomType(product, ChatRoomType.AFTER_TRADE);

        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            return new ChatRoomCreateResponseDto(
                    room.getId(), room.getRoomType(), room.getName(), room.isClosed()
            );
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .product(product)
                .roomType(ChatRoomType.AFTER_TRADE)
                .isClosed(false)
                .build();

        chatRoomRepository.save(chatRoom);


        chatParticipantService.addParticipant(chatRoom, product.getSeller());
        chatParticipantService.addParticipant(chatRoom, transaction.getBuyer());

        return new ChatRoomCreateResponseDto(
                chatRoom.getId(),
                chatRoom.getRoomType(),
                chatRoom.getName(),
                chatRoom.isClosed()
        );
    }


    // 특정 사용자의 채팅방 목록 전체 조회하기
    public List<ChatRoomResponseDto> getUserChatRooms(User user) {
        // 페치조인으로 필요한정보 다가져오기
        List<ChatParticipant> cps = chatParticipantRepository.findAllWithRoomAndProductByUser(user);

        // 방 아이디 목록추출
        List<Long> roomIds = cps.stream()
                .map(cp -> cp.getChatRoom().getId())
                .toList();

        // 방별 미읽음 개수 가져오기
        Map<Long, Long> unreadMap = roomIds.isEmpty()
                ? Map.of()
                : readStatusRepository.countUnreadByRoomIds(user.getId(), roomIds)
                .stream()
                .collect(Collectors.toMap(
                        dto -> dto.getRoomId(),
                        dto -> dto.getCnt()
                ));

        // DTO 매핑
        return cps.stream()
                .map(cp -> {
                    ChatRoom room = cp.getChatRoom();
                    Product product = room.getProduct();
                    long unread = unreadMap.getOrDefault(room.getId(), 0L);

                    return new ChatRoomResponseDto(
                            room.getId(),
                            room.getName(),
                            room.getRoomType(),
                            room.isClosed(),
                            product.getTitle(),
                            product.getSeller().getNickname(),
                            unread
                    );
                })
                .toList();
    }

    //채팅방 종료
    @Transactional
    public void closeRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 참여자인지 확인
        boolean isParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, currentUser).isPresent();
        if (!isParticipant) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }


        chatRoom.closeRoom();
    }

}
