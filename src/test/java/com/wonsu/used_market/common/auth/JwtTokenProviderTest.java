package com.wonsu.used_market.common.auth;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    //토큰생성 테스트
    @Test
    public void create_jwt_success() throws Exception{
        // given
        String email = "sso@email.com";
        String role = "USER";

        // when
        String token = jwtTokenProvider.createToken(email, role);

        // then
        jwtTokenProvider.validateToken(token);
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo(email);
    }



}