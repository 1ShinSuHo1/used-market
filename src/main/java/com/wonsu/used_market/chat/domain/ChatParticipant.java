package com.wonsu.used_market.chat.domain;

import com.wonsu.used_market.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "chat_participant")
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    //읽지 않은 메시지의 수
    @Column(nullable = false)
    private int unreadCount = 0;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //메시지를 읽으면 읽지 않은 개수를 초기화
    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    //새로운 메시지가 도착하면 +1 증가
    public void increaseUnreadCount() {
        this.unreadCount++;
    }

    // 참여자 연결
    public void assignToRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (!chatRoom.getParticipants().contains(this)) {
            chatRoom.getParticipants().add(this);
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    private ChatParticipant(ChatRoom chatRoom, User user, int unreadCount) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.unreadCount = unreadCount;
    }


}
