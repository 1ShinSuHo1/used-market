package com.wonsu.used_market.user.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.dto.UserCreateDto;
import com.wonsu.used_market.user.dto.UserLoginDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    // 회원가입 확인
    @Test
    public void register_success() throws Exception{

            // given
            UserCreateDto dto = new UserCreateDto(
                    "test@email.com",
                    "password1234",
                    "sso",
                    "수호",
                    null,
                    "01012345678",
                    "경기도"
            );

            // when
            User user = userService.create(dto);

            // then
            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail()).isEqualTo(dto.getEmail());
            assertThat(user.isEmailVerified()).isTrue();

    }

    // 회원가입시 이메일 중복검사 확인 및 예외처리 확인
    @Test
    public void register_fail_duplicate_email() throws Exception{
        // given
        UserCreateDto dto1 = new UserCreateDto("sso@email.com", "pw123456", "nick1", null, null, null, null);
        UserCreateDto dto2 = new UserCreateDto("sso@email.com", "pw999999", "nick2", null, null, null, null);

        userService.create(dto1);

        // when

        // then
        assertThatThrownBy(() -> userService.create(dto2))
                .isInstanceOf(BusinessException.class);
    }

    //로그인 테스트
    @Test
    public void login_success() throws Exception{
        // given
        UserCreateDto createDto = new UserCreateDto("login@test.com", "pw123456", "nick123", null, null, null, null);
        userService.create(createDto);

        UserLoginDto loginDto = new UserLoginDto("login@test.com", "pw123456");

        // when
        User loginUser = userService.login(loginDto);

        // then
        assertThat(loginUser).isNotNull();
        assertThat(loginUser.getEmail()).isEqualTo("login@test.com");
    }

}