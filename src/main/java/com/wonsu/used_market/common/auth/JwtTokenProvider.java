package com.wonsu.used_market.common.auth;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    //인코딩된 시크릿키
    private final String secretKey;

    private final int expiration;

    //어떤 암호화를 쓸껀지 설정된 시크릿키
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,@Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;

        //디코딩 시키고 디코딩된문자열을 샤 알고리즘 적용
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role) {
        // claims는 jwt토큰의 payload부분을 의미
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L)) //gettime은 밀리초 단위다
                .signWith(SECRET_KEY)
                .compact();
        return token;
    }

    //유효성 위조 만료 검사
    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.JWT_EXPIRED, e);
        } catch (UnsupportedJwtException e) {
            throw new BusinessException(ErrorCode.JWT_UNSUPPORTED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.JWT_INVALID, e);
        }
    }

    //페이로드에서 정보 파싱
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
    public String getRole(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build()
                .parseClaimsJws(token).getBody().get("role", String.class);
    }


}
