package com.wonsu.used_market.user.controller;


import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.UserResponseDto;
import com.wonsu.used_market.user.dto.UserUpdateRequestDto;
import com.wonsu.used_market.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal UserDetails principal) {
        String email = principal.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(new UserResponseDto(user));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(@AuthenticationPrincipal UserDetails principal, @RequestBody UserUpdateRequestDto dto){
        String email = principal.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        User updateUser = userService.updateUser(user.getId(), dto);

        return ResponseEntity.ok(new UserResponseDto(updateUser));
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyInfo(@AuthenticationPrincipal UserDetails principal) {
        String email = principal.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userService.deleteUser(user.getId());

        return ResponseEntity.noContent().build();
    }




}
