package com.sparta.sportify.jwt;

import com.sparta.sportify.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.rmi.RemoteException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 Bearer 토큰을 추출
        String token = jwtUtil.resolveToken(request);
        if (token != null && jwtUtil.validateToken(token)!=null) {
            String username = jwtUtil.getUsernameFromToken(token);  // 토큰에서 username 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);  // email 대신 username으로 처리

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 인증 정보를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 삭제된 사용자 확인
            if (((UserDetailsImpl) userDetails).getUser().getDeletedAt() != null) {
                throw new RemoteException("삭제된 유저입니다.");  // 삭제된 사용자 예외 처리

            }
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

//    private OAuthUserInfo getUserInfoFromKakao(String token) {
//        // 카카오 API 호출 및 사용자 정보 추출 로직
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
//                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, entity, KakaoUserResponse.class);
//
//        if (response.getStatusCode() != HttpStatus.OK) {
//            throw new IllegalArgumentException("카카오 OAuth 인증 실패: 잘못된 Access Token");
//        }
//
//        KakaoUserResponse userResponse = response.getBody();
//        if (userResponse == null || userResponse.getKakaoAccount() == null || userResponse.getProperties() == null) {
//            throw new IllegalArgumentException("카카오 사용자 정보를 가져오지 못했습니다.");
//        }
//
//        // 사용자 정보 매핑
//        KakaoUserResponse.KakaoAccount kakaoAccount = userResponse.getKakaoAccount();
//        KakaoUserResponse.Properties properties = userResponse.getProperties();
//
//        String email = kakaoAccount.getEmail();
//        if (email == null || email.isBlank()) {
//            throw new RuntimeException("사용자의 이메일 정보가 없습니다.");
//        }
//
//        String nickname = properties.getNickname();
//        if (nickname == null || nickname.isBlank()) {
//            nickname = "사용자"; // 닉네임이 없을 경우 기본값 설정
//        }
//
//        String gender = kakaoAccount.getGender();
//        String ageRange = kakaoAccount.getAgeRange();
//
//        // 안전한 age 변환
//        Long age = null;
//        try {
//            if (ageRange != null && ageRange.contains("~")) {
//                age = Long.valueOf(ageRange.split("~")[0].trim());
//            }
//        } catch (NumberFormatException e) {
//            log.warn("AgeRange 변환 중 오류 발생: {}", ageRange);
//        }
//
//        OAuthUserInfo userInfo = new OAuthUserInfo(email, nickname);
//        userInfo.setEmail(email);
//        userInfo.setName(nickname);
//
//
//        return userInfo;
//    }


    private boolean isKakaoToken(String token) {
        // 카카오 토큰인지 검사하는 로직
        return token != null && token.startsWith("Kakao ");
    }
}
