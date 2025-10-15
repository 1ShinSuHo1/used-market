package com.wonsu.used_market.transaction.controller;

import com.wonsu.used_market.common.auth.CurrentUser;
import com.wonsu.used_market.transaction.dto.TransactionResponseDto;
import com.wonsu.used_market.transaction.service.TransactionService;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 거래 요청(구매자가 거래요청 버튼 클릭)
    @PostMapping("/request/{productId}")
    public ResponseEntity<TransactionResponseDto> requestTransaction(
            @PathVariable Long productId,
            @RequestParam(required = false) Long chatRoomId,
            @CurrentUser User buyer
            ){
        return ResponseEntity.ok(
                transactionService.requestTransaction(productId,chatRoomId,buyer)
        );
    }

    // 거래 수락(판매자가 거래수락 버튼 클릭)
    @PostMapping("/{transactionId}/accept")
    public ResponseEntity<TransactionResponseDto> acceptTransaction(
            @PathVariable Long transactionId,
            @CurrentUser User seller) {

        TransactionResponseDto response = transactionService.acceptTransaction(transactionId, seller);
        return ResponseEntity.ok(response);
    }

    //거래 완료(구매자 또는 판매자가 거래완료 버튼 호출)
    @PostMapping("/{transactionId}/complete")
    public ResponseEntity<TransactionResponseDto> completeTransaction(
            @PathVariable Long transactionId,
            @CurrentUser User user) {

        TransactionResponseDto response = transactionService.completeTransaction(transactionId, user);
        return ResponseEntity.ok(response);
    }

    //거래취소(구매자 도는 판매자가 거래취소)
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<TransactionResponseDto> cancelTransaction(
            @PathVariable Long transactionId,
            @CurrentUser User user) {

        TransactionResponseDto response = transactionService.cancelTransaction(transactionId, user);
        return ResponseEntity.ok(response);
    }

    //내 거래 내역 조회
    @GetMapping("/my")
    public ResponseEntity<Page<TransactionResponseDto>> getMyTransactions(
            @CurrentUser User currentUser,
            Pageable pageable) {

        Page<TransactionResponseDto> response = transactionService.getMyTransactions(currentUser, pageable);
        return ResponseEntity.ok(response);
    }
}
