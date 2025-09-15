package com.wonsu.used_market.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users",
        uniqueConstraints = {
                //이메일과,닉네임 중복 방지,프로바이더와 프로바이더아이디 조합도 중복안됨
                @UniqueConstraint(name = "ux_users_email", columnNames = {"email"}),
                @UniqueConstraint(name = "ux_users_nickname", columnNames = {"nickname"}),
                @UniqueConstraint(name = "ux_oauth_provider_id", columnNames = {"provider", "provider_id"})

        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private  String email;

    @Column(name = "password")
    private  String password;

    @Column(nullable = false)
    private  String nickname;

    @Column(length = 40)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;

    @Column(name = "addr")
    private String address;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    //빌더를 통해 객체를 이름있는 파라미터 방식으로 생성
    @Builder
    private User(
            String email,
            String password,
            String nickname,
            String name,
            LocalDate birthDate,
            String phone,
            String address,
            Provider provider,
            String providerId,
            Role role
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        this.provider = (provider != null) ? provider : Provider.LOCAL;
        this.providerId = providerId;
        this.role = (role != null) ? role : Role.USER;
        this.emailVerified = false;
    }

    //Setter를 안쓰기 위해 메서드로 대체하기
    public void verifyEmail() {
        this.emailVerified = true;
    }
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePhone(String newPhone) {
        this.phone = newPhone;
    }

    public void changeAddr(String newAddr) {
        this.address = newAddr;
    }

    // 엔티티 라이프 사이클
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
