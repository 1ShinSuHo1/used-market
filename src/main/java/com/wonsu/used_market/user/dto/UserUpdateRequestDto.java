package com.wonsu.used_market.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @Size(max = 40, message = "닉네임은 최대 40자까지 입력 가능합니다.")
    private String nickname;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "전화번호는 숫자 10~20자리여야 합니다.")
    private String phone;

    @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    private String addr;

    @Size(max = 40, message = "이름은 최대 40자까지 입력 가능합니다.")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String password;
}
