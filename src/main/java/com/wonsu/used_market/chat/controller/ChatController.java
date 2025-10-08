package com.wonsu.used_market.chat.controller;

import com.wonsu.used_market.chat.dto.ChatMessageResponseDto;
import com.wonsu.used_market.chat.dto.ChatRoomCreateResponseDto;
import com.wonsu.used_market.chat.dto.ChatRoomResponseDto;
import com.wonsu.used_market.chat.service.ChatMessageService;
import com.wonsu.used_market.chat.service.ChatRoomService;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    //내 채팅방 목록조회
    @GetMapping("/rooms/my")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        User me = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(chatRoomService.getUserChatRooms(me));
    }

    //특정 방의 메시지 조회 asc로 정렬하였음
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getRoomMessages(@PathVariable Long roomId, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        return ResponseEntity.ok(chatMessageService.getChatHistory(roomId));
    }

    //해당방의 안읽은 메시지 전체읽음 처리
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long roomId, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        Long userId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        chatMessageService.markAllAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    //일반 거래 채팅방 개설 있으면 재사용
    @PostMapping("/rooms/direct")
    public ResponseEntity<ChatRoomCreateResponseDto> openDirectRoom(@RequestParam Long productId, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        Long buyerId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        return ResponseEntity.ok(chatRoomService.createDirectRoom(productId, buyerId));
    }

    //경매전에 문의 채팅방 개설 있으면 재사용
    @PostMapping("/rooms/auction-inquiry")
    public ResponseEntity<ChatRoomCreateResponseDto> openAuctionInquiryRoom(@RequestParam Long productId, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        Long userId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        return ResponseEntity.ok(chatRoomService.createAuctionInquiryRoom(productId, userId));
    }

    // 방 종료하기
    @PostMapping("/rooms/{roomId}/close")
    public ResponseEntity<?> closeRoom(@PathVariable Long roomId, Principal principal) {
        if (principal == null) throw new BusinessException(ErrorCode.JWT_INVALID);
        Long userId = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getId();
        chatRoomService.closeRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }
}
