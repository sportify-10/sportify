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
        // OAuth2User 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 플랫폼별 사용자 ID (oauthId) 가져오기
        String oauthId = extractOauthId(oAuth2User);

        // 사용자 정보 확인 또는 등록
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 사용자 정보가 없습니다: " + oauthId));

        // JWT 토큰 생성
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 응답을 JSON 형식으로 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"jwt\": \"" +"Bearer "+ jwt + "\"}");
        response.getWriter().flush();
    }

    private String extractOauthId(OAuth2User oAuth2User) {
        // OAuth2User로부터 플랫폼별 사용자 ID 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("sub")) {
            return (String) attributes.get("sub"); // Google
        } else if (attributes.containsKey("id")) {
            return String.valueOf(attributes.get("id")); // Kakao
        } else if (attributes.containsKey("response")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response"); // Naver
            return (String) response.get("id");
        }
        throw new IllegalArgumentException("OAuth2 사용자 ID를 추출할 수 없습니다.");
    }
}
