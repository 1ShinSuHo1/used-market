package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.ChatRoomType;
import com.wonsu.used_market.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    //상품과 채팅방 타입으로 채팅방 찾기
    Optional<ChatRoom> findByProductAndRoomType(Product product, ChatRoomType roomType);

    // 상품의 모든 채팅방
    List<ChatRoom> findByProduct(Product product);
}
