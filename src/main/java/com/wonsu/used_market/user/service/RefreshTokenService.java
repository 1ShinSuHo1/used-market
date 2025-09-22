package com.wonsu.used_market.user.service;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    //Redis 네이밍 컨벤션
    private static final String PREFIX = "refresh_token:";



    //리프레쉬 토큰 저장
    public void saveRefreshToken(Long userId, String token) {
        String key = PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, token, refreshExpiration, TimeUnit.MINUTES);
    }

    //리프레쉬 토큰 조회
    public String getRefreshToken(Long userId) {
        String key = PREFIX + userId;
        String token = stringRedisTemplate.opsForValue().get(key);
        if (token == null) {
            throw new BusinessException(ErrorCode.JWT_INVALID); //만료의 경우나 없는경우
        }
        return token;
    }

    //리프레쉬 토큰 삭제
    public void deleteRefreshToken(Long userId) {
        String key = PREFIX + userId;
        stringRedisTemplate.delete(key);
    }


}
