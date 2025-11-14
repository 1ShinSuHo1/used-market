package com.wonsu.used_market.common.auth;

import com.wonsu.used_market.exception.BusinessException;
import com.wonsu.used_market.exception.ErrorCode;
import com.wonsu.used_market.user.domain.User;
import com.wonsu.used_market.user.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class JwtTokenFilter extends GenericFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String token = httpServletRequest.getHeader("Authorization");

        try {
            if (token != null) {
                if (!token.startsWith("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다");
                }
                String jwtToken = token.substring(7);

                jwtTokenProvider.validateToken(jwtToken);

                String email = jwtTokenProvider.getEmail(jwtToken);

                // DB 조회
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                // CustomUserDetails 생성
                CustomUserDetails userDetails = new CustomUserDetails(user);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            chain.doFilter(request, response);
        } catch (BusinessException e) {
            handleErrorResponse(httpServletResponse, e.getErrorCode().getStatus().value(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            handleErrorResponse(httpServletResponse, HttpStatus.UNAUTHORIZED.value(), "invalid token");
        }
    }

    // 예외 응답 처리메서드
    private void handleErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        if (!response.isCommitted()) {
            //이미 응답이 전송되었는가
            response.setStatus(status);
            response.setContentType("application/json;charset=utf-8");

            // 단순 문자열이 아니라 JSON 형식으로 에러 반환
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        }
    }

}

