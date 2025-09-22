package com.wonsu.used_market.user.dto;

import com.wonsu.used_market.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String name;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private boolean emailVerified;
    private String provider;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.name = user.getName();
        this.birthDate = user.getBirthDate();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.emailVerified = user.isEmailVerified();
        this.provider = user.getProvider().toString();
        this.role = user.getRole().toString();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
