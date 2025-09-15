package com.wonsu.used_market.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder makePassword() {
        //반환되는 객체를 스프링 빈으로 등록한다
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
                .authorizeHttpRequests(a -> a.requestMatchers("/auth/register","/auth/login").permitAll().anyRequest().authenticated())
                .build();
    }

    // 프론트서버와 맞춰주기 위해서
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(Arrays.asList("*")); //모든 HTTP 메서드 허용
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); //모든 헤더값을 허용
        corsConfiguration.setAllowCredentials(true); //자격증명허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //모든 url패턴에 대해 cors허용 설정
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
