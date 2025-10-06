package com.wonsu.used_market.chat.domain;

import com.wonsu.used_market.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    //채팅방 유형
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType roomType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isClosed = false;

    @OneToMany(mappedBy = "chatRoom",cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 경매가 시작되거나 거래가 완료되면 채팅방을 닫아버려야함
    public void closeRoom() {
        this.isClosed = true;
    }

    public boolean isActive(){
        return !isClosed();
    }

    //채팅방 이름 자동생성 (상품명과 티입기반으로 생성)
    private String generateRoomName(Product product,ChatRoomType roomType) {
        String title = Optional.ofNullable(product)
                .map(Product::getTitle)
                .orElse("거래");


        return switch (roomType){
            case DIRECT -> title + " 거래방";
            case AUCTION_INQUIRY -> "[경매 문의] " + title;
            case AFTER_TRADE -> "[낙찰 거래] " + title;
        };
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    private ChatRoom(Product product, ChatRoomType roomType, boolean isClosed){
        this.product = product;
        this.roomType = roomType;
        this.isClosed = isClosed;
        this.name = generateRoomName(product, roomType);
    }
}
