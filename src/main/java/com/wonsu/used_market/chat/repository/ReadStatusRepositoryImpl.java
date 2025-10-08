package com.wonsu.used_market.chat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wonsu.used_market.chat.domain.QReadStatus;
import com.wonsu.used_market.chat.dto.RoomUnreadCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;

import java.util.List;

@RequiredArgsConstructor
public class ReadStatusRepositoryImpl implements ReadStatusRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomUnreadCountDto> countUnreadByRoomIds(Long userId, List<Long> roomIds) {
        QReadStatus rs = QReadStatus.readStatus;

        return queryFactory
                .select(Projections.constructor(
                        RoomUnreadCountDto.class,
                        rs.chatRoom.id,
                        rs.id.count()
                ))
                .from(rs)
                .where(
                        rs.user.id.eq(userId),
                        rs.isRead.isFalse(),
                        rs.chatRoom.id.in(roomIds)
                )
                .groupBy(rs.chatRoom.id)
                .fetch();
    }
}
