package com.wonsu.used_market.user.controller;


import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.UserCreateDto;
import com.wonsu.used_market.user.dto.UserLoginDto;
import com.wonsu.used_market.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = userService.create(userCreateDto);
        return new ResponseEntity<>(user.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@Valid @RequestBody UserLoginDto userLoginDto) {
        // email, password 일치한지 검증
        User user = userService.login(userLoginDto);

        // 일치할 경우 jwt accesstoken 생성
        String jwtToken = jwtTokenProvider.createToken(user.getEmail(),user.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", user.getId());
        loginInfo.put("token", jwtToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }
    
}
