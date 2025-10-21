package com.wonsu.used_market.common.config;

import com.wonsu.used_market.common.auth.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public PasswordEncoder makePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain myfilter(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)//csrf비활성화 (MVC패턴이 아니기때문에)
                // Basic 인증 비활성화
                // Basic 인증은 사용자이름과 비밀번호를 Base64로 인코하여 인증값으로 활용
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션방식을 비활성화
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 특정 url 패턴에 대해서는 인증처리(Authentication 객체생성) 제외
                .authorizeHttpRequests(a -> a.requestMatchers("/auth/register","/auth/login","/auth/oauth/google/login","/auth/oauth/kakao/login","/auth/refresh","/connect/**","/uploads/**").permitAll().anyRequest().authenticated())
                // UsernamePasswordAuthenticationFilter 이클래스에서 폼로그인 인증을 처리
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 프론트와 연결을 위해
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOriginPattern("*"); // ngrok을 사용하기위해 설정
        corsConfiguration.setAllowedMethods(Arrays.asList("*")); //모든 HTTP 메서드 허용
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); //모든 헤더값을 허용
        corsConfiguration.setAllowCredentials(true); //자격증명허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //모든 url패턴에 대해 cors허용 설정
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
