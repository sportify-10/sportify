package com.sparta.sportify.security;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil; // JWT 유틸리티
    private final UserRepository userRepository; // 사용자 저장소

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();

        // 사용자 등록 또는 업데이트 로직 (userRepository 사용)
        User user = userRepository.findByOauthId(customUser.getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자 정보가 없습니다: " + customUser.getProviderId()));

        // JWT 생성
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // JSON 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"jwt\": \"" + "Bearer " + jwt + "\"}");
        response.getWriter().flush();
    }


}
