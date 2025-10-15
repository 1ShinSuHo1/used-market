package com.wonsu.used_market.transaction.service;

import com.wonsu.used_market.chat.service.ChatRoomService;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.product.repository.ProductRepository;
import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.transaction.dto.TransactionResponseDto;
import com.wonsu.used_market.transaction.repository.TransactionRepository;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final ChatRoomService chatRoomService;

    //채팅방에서 거래 요청 버튼을 눌렀을시에 호출
    @Transactional
    public TransactionResponseDto requestTransaction(Long productId,Long chatRoomId, User buyer){
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User seller = product.getSeller();

        if(seller.equals(buyer)){
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        // 중복 거래 방지
        if (transactionRepository.existsByProductId(product.getId())) {
            throw new BusinessException(ErrorCode.TRANSACTION_ALREADY_EXISTS);
        }

        Transaction transaction = Transaction.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .priceFinal(product.getPrice())
                .chatRoomId(chatRoomId)
                .completedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("[TRANSACTION REQUESTED] productId={}, buyer={}, seller={}, chatRoom={}",
                productId, buyer.getNickname(), seller.getNickname(), chatRoomId);

        return TransactionResponseDto.from(transaction);


    }

    //판매자가 거래를 수락 PENDING -> CONFIRMED
    @Transactional
    public TransactionResponseDto acceptTransaction(Long transactionId, User seller){
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!tx.getSeller().equals(seller)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        tx.confirm();
        log.info("[TRANSACTION ACCEPTED] id={}, seller={}", tx.getId(), seller.getNickname());
        return TransactionResponseDto.from(tx);
    }

    //거래 완료 CONFIRMED -> COMPLETED
    @Transactional
    public TransactionResponseDto completeTransaction(Long transactionId, User user) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!tx.getSeller().equals(user) && !tx.getBuyer().equals(user)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        tx.complete();
        log.info("[TRANSACTION COMPLETED] id={}, user={}", tx.getId(), user.getNickname());
        return TransactionResponseDto.from(tx);
    }


    // 거래 생성
    @Transactional
    public TransactionResponseDto createTransaction(Product product, User buyer, User seller, Integer priceFinal) {
        // 동일상품의 거래 있으면 예외발생
        if (transactionRepository.existsByProductId(product.getId())) {
            throw new BusinessException(ErrorCode.TRANSACTION_ALREADY_EXISTS);
        }

        Transaction transaction = Transaction.builder()
                .product(product)
                .buyer(buyer)
                .seller(seller)
                .priceFinal(priceFinal)
                .completedAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        log.info("[TRANSACTION CREATED] productId={}, buyer={}, seller={}, price={}",
                product.getId(), buyer.getNickname(), seller.getNickname(), priceFinal);

        //경매후 연결 채팅방 생성
        chatRoomService.createAfterTradeRoom(transaction);
        log.info("[AUTO CHAT ROOM CREATED AFTER AUCTION] productId={}, buyer={}, seller={}",
                product.getId(), buyer.getNickname(), seller.getNickname());

        return TransactionResponseDto.from(transaction);

    }




    // 거래취소
    @Transactional
    public TransactionResponseDto cancelTransaction(Long transactionId, User user) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (!tx.getSeller().equals(user) && !tx.getBuyer().equals(user)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        tx.cancel();
        log.info("[TRANSACTION CANCELED] id={}, user={}", tx.getId(), user.getNickname());
        return TransactionResponseDto.from(tx);
    }

    public Page<TransactionResponseDto> getMyTransactions(User currentUser, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAllByUserWithFetch(currentUser, pageable);
        return transactions.map(TransactionResponseDto::from);
    }
}
