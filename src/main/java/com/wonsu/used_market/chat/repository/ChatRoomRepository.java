package com.wonsu.used_market.chat.repository;

import com.wonsu.used_market.chat.domain.ChatRoom;
import com.wonsu.used_market.chat.domain.ChatRoomType;
import com.wonsu.used_market.product.domain.Product;
import com.wonsu.used_market.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    //상품과 채팅방 타입으로 채팅방 찾기
    Optional<ChatRoom> findByProductAndRoomType(Product product, ChatRoomType roomType);

    // 상품과 타입 특정유저 기준으로 방 찾기
    @Query("""
        select r
        from ChatRoom r
        join ChatParticipant cp on cp.chatRoom = r
        where r.product = :product
          and r.roomType = :roomType
          and cp.user = :user
    """)
    Optional<ChatRoom> findByProductAndRoomTypeAndUser(
            Product product,
            ChatRoomType roomType,
            User user
    );
    // 상품의 모든 채팅방
    List<ChatRoom> findByProduct(Product product);
}
