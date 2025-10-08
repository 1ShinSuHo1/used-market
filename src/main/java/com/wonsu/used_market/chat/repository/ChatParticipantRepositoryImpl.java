package com.wonsu.used_market.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.chat.domain.ChatParticipant;
import com.wonsu.used_market.chat.domain.QChatParticipant;
import com.wonsu.used_market.chat.domain.QChatRoom;
import com.wonsu.used_market.product.domain.QProduct;
import com.wonsu.used_market.user.domain.QUser;
import com.wonsu.used_market.user.domain.User;
import lombok.RequiredArgsConstructor;


import java.util.List;


@RequiredArgsConstructor
public class ChatParticipantRepositoryImpl implements ChatParticipantRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatParticipant> findAllWithRoomAndProductByUser(User user) {
        QChatParticipant cp = QChatParticipant.chatParticipant;
        QChatRoom room = QChatRoom.chatRoom;
        QProduct product = QProduct.product;
        QUser seller = QUser.user;

        return queryFactory
                .selectFrom(cp)
                .join(cp.chatRoom, room).fetchJoin()
                .join(room.product, product).fetchJoin()
                .join(product.seller, seller).fetchJoin()
                .where(cp.user.eq(user))
                .fetch();

    }
}
