package com.wonsu.used_market.transaction.repository;

import com.wonsu.used_market.transaction.domain.Transaction;
import com.wonsu.used_market.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionRepositoryCustom {

    //특정 유저의 거래 목록 조회
    List<Transaction> findAllByUserWithFetch(User user);

    //특정 유저의 거래목록을 페이징 형태로조회
    Page<Transaction> findAllByUserWithFetch(User user, Pageable pageable);

    //특정 상품에 이미 거래가 존재하는지 중복체크
    boolean existsByProductId(Long productId);

}
