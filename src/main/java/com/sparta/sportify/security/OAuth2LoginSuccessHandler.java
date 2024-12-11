package com.sparta.sportify.security;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성기 (구현 필요)
    private final UserRepository userRepository; // 사용자 정보 확인

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 사용자 정보 가져오기
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String oauthId = oAuth2User.getName(); // OAuth2 사용자 고유 ID

        // 사용자 확인 및 JWT 토큰 생성
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자 정보가 없습니다: " + oauthId));

        // JWT 생성 및 응답 헤더에 추가
        String token = jwtTokenProvider.generateToken(user);

        response.addHeader("Authorization", "Bearer " + token);

        // 리다이렉트 처리
        getRedirectStrategy().sendRedirect(request, response, "/"); // 메인 페이지로 리다이렉트
    }
}
