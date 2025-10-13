package com.wonsu.used_market.common.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

//컨트롤러에서 로그인한사용자를 바로 주입받기 위해서 커스텀 애노테이션을 작성
@Target(ElementType.PARAMETER) //메서드의 파라미터에만 사용 가능하게 지정
@Retention(RetentionPolicy.RUNTIME) // 런타임에도 유지
@Documented
//스프링 세큐리티가 authentication안에 principal을 자동으로 찾아 컨트롤러의 파라미터로 주입해주는기능을함
//.getPrincipal호출하고 CustomUserDetails타입인지 확인하고, 유저 반환하고, 컨트롤러 파라미터에 주입
@AuthenticationPrincipal(expression = "user")
public @interface CurrentUser {
}
