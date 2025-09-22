package com.wonsu.used_market.user.service;

import com.wonsu.used_market.common.auth.JwtTokenProvider;
import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //리프레쉬 토큰 저장 및 조회 성공
    @Test
    public void saveAndGetRefreshToken() throws Exception{
        // given
        Long userId = 1L;
        String refreshToken = jwtTokenProvider.createRefreshToken("test@test.com", "USER");

        // when
        refreshTokenService.saveRefreshToken(userId, refreshToken);
        String storedToken = refreshTokenService.getRefreshToken(userId);

        // then
        assertThat(storedToken).isEqualTo(refreshToken);
    }

    //리프레쉬 토큰 삭제 잘되는지 확인
    @Test
    public void deleteRefreshToken() throws Exception{
        // given
        Long userId = 2L;
        String refreshToken = jwtTokenProvider.createRefreshToken("delete@test.com", "USER");
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        // when
        refreshTokenService.deleteRefreshToken(userId);

        // then
        assertThatThrownBy(() -> refreshTokenService.getRefreshToken(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.JWT_INVALID.getMessage());
    }
}