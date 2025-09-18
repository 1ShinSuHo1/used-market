package com.wonsu.used_market.user.controller;


import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.user.domain.Provider;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.*;
import com.wonsu.used_market.user.service.GoogleService;
import com.wonsu.used_market.user.service.KakaoService;
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
    private final GoogleService googleService;
    private final KakaoService kakaoService;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, GoogleService googleService, KakaoService kakaoService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleService = googleService;
        this.kakaoService = kakaoService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user =  userService.create(userCreateDto);
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

    @PostMapping("/oauth/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        // accesstoken 발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

        // 사용자 정보 얻기
        GoogleProfileDto googleProfileDto =  googleService.getGoogleProfile(accessTokenDto.getAccess_token());
        //회원가입이 되어 있지 않다면 회원가입
        User originalUser = userService.getUserByProviderId(Provider.GOOGLE, googleProfileDto.getSub());
        if(originalUser == null){
            originalUser = userService.createOauth(googleProfileDto.getSub(),googleProfileDto.getEmail(), Provider.GOOGLE);
        }
        //회원가입이 되어있는 회원이라면 토큰발급
        String jwtToken = jwtTokenProvider.createToken(originalUser.getEmail(),originalUser.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalUser.getId());
        loginInfo.put("token", jwtToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);

    }

    @PostMapping("/oauth/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());

        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        User originalUser = userService.getUserByProviderId(Provider.KAKAO, kakaoProfileDto.getId());
        if(originalUser == null){
            originalUser = userService.createOauth(kakaoProfileDto.getId(),kakaoProfileDto.getKakao_account().getEmail(), Provider.KAKAO);
        }

        String jwtToken = jwtTokenProvider.createToken(originalUser.getEmail(),originalUser.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalUser.getId());
        loginInfo.put("token", jwtToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);

    }
    
}
