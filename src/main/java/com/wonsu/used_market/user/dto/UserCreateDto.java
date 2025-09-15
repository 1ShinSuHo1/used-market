package com.wonsu.used_market.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 255, message = "이메일은 최대 255자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 40, message = "닉네임은 최대 40자까지 입력 가능합니다.")
    private String nickname;

    @Size(max = 40, message = "이름은 최대 40자까지 입력 가능합니다.")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
    private String phone;

    @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    private String address;
}
