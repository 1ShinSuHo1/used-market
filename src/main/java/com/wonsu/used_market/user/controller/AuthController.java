package com.wonsu.used_market.user.controller;


import com.wonsu.used_market.common.auth.CustomUserDetails;
import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.Provider;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.*;
import com.wonsu.used_market.user.service.GoogleService;
import com.wonsu.used_market.user.service.KakaoService;
import com.wonsu.used_market.user.service.RefreshTokenService;
import com.wonsu.used_market.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider, GoogleService googleService, KakaoService kakaoService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleService = googleService;
        this.kakaoService = kakaoService;
        this.refreshTokenService = refreshTokenService;

    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user =  userService.create(userCreateDto);
        return new ResponseEntity<>(user.getId(), HttpStatus.CREATED);
    }

    //일반 로그인
    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@Valid @RequestBody UserLoginDto userLoginDto) {
        // 1.email, password 일치한지 검증
        User user = userService.login(userLoginDto);

        // 2. 일치할 경우 jwt accesstoken 생성 및 RefreshToken발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().toString());

        // 3. Redis에 리프레쉬 토큰 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", user.getId());
        loginInfo.put("accessToken", accessToken);
        loginInfo.put("refreshToken", refreshToken);

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
            boolean emailVerified = googleProfileDto.isEmail_verified();
            originalUser = userService.createOauth(googleProfileDto.getSub(),googleProfileDto.getEmail(), Provider.GOOGLE, emailVerified );
        }

        //회원가입이 되어있는 회원이라면 토큰발급
        String accessToken = jwtTokenProvider.createAccessToken(originalUser.getEmail(), originalUser.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(originalUser.getEmail(), originalUser.getRole().toString());

        refreshTokenService.saveRefreshToken(originalUser.getId(), refreshToken);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalUser.getId());
        loginInfo.put("accessToken", accessToken);
        loginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);

    }

    @PostMapping("/oauth/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());

        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        User originalUser = userService.getUserByProviderId(Provider.KAKAO, kakaoProfileDto.getId());
        if(originalUser == null){
            boolean emailVerified = kakaoProfileDto.getKakao_account().is_email_verified();
            originalUser = userService.createOauth(kakaoProfileDto.getId(),kakaoProfileDto.getKakao_account().getEmail(), Provider.KAKAO , emailVerified);
        }

        String accessToken = jwtTokenProvider.createAccessToken(originalUser.getEmail(), originalUser.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(originalUser.getEmail(), originalUser.getRole().toString());

        refreshTokenService.saveRefreshToken(originalUser.getId(), refreshToken);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalUser.getId());
        loginInfo.put("accessToken", accessToken);
        loginInfo.put("refreshToken", refreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);

    }

    //토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();
        log.info("👉 Client refreshToken: {}", refreshToken);

        try {
            jwtTokenProvider.validateToken(refreshToken); // 만료/위조 검사
        } catch (BusinessException e) {
            log.error("Token validation failed: {}", e.getErrorCode().getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode().getMessage());
        }

        // 토큰에서 정보 추출
        String email = jwtTokenProvider.getEmail(refreshToken);
        String role = jwtTokenProvider.getRole(refreshToken);

        // 유저 확인
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Redis에 저장된 refresh token과 일치하는지 검증
        String storedRefreshToken = refreshTokenService.getRefreshToken(user.getId());
        if (!storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.JWT_INVALID);
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(email, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email, role);

        // Redis에 갱신 (덮어쓰기)
        refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        return ResponseEntity.ok(tokens);
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        refreshTokenService.deleteRefreshToken(user.getId());

        return ResponseEntity.ok(Map.of("success", true));
    }



}
