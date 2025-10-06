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
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(length = 1000)
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void assignToRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        chatRoom.getMessages().add(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    private ChatMessage(ChatRoom chatRoom, User sender, MessageType type, String content, String fileUrl) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.type = (type != null) ? type : MessageType.TEXT;
        this.content = content;
        this.fileUrl = fileUrl;

    }
}
