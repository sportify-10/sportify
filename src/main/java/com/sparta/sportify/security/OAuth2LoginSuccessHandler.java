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

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성기
    private final UserRepository userRepository; // 사용자 정보 확인

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String oauthId = oAuth2User.getAttribute("sub");

        // 사용자 확인
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자 정보가 없습니다: " + oauthId));

        // JWT 생성
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 응답 바디에 JWT 포함
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"jwt\": \"" +"Bearer " + token + "\"}");
        writer.flush();
    }
}
