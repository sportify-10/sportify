package com.sparta.sportify.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.user.req.OAuthUserInfo;
import com.sparta.sportify.dto.user.res.KakaoUserResponse;
import com.sparta.sportify.dto.user.res.OAuthResponseDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String KAKAO_API_URL = "https://kapi.kakao.com/v2/user/me";
    private final RestTemplate restTemplate;

    @Override
    public OAuthResponseDto login(String accessToken) {
        // 1. Access Token 유효성 검증 및 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(KAKAO_API_URL, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("유효하지 않은 카카오 액세스 토큰");
        }

        // 2. 사용자 정보 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode userInfo;
        try {
            userInfo = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 파싱 실패");
        }

        String email = userInfo.get("kakao_account").get("email").asText();
        String name = userInfo.get("properties").get("nickname").asText();

        // 3. 회원 정보 확인 및 신규 사용자 생성
        User user = userRepository.findByEmail(email).orElseGet(() -> createNewUser(email, name));

        // 4. JWT 토큰 발급
        String token = jwtTokenProvider.generateToken(user);

        return new OAuthResponseDto(email, name, token);
    }

    /**
     * Access Token을 사용해 사용자 정보 가져오기
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        // 카카오 API 호출을 위한 HTTP 요청 처리
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                KAKAO_API_URL, HttpMethod.GET, entity, KakaoUserResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("카카오 OAuth 인증 실패: 잘못된 Access Token");
        }

        KakaoUserResponse userResponse = response.getBody();
        if (userResponse == null || userResponse.getKakaoAccount() == null) {
            throw new IllegalArgumentException("카카오 사용자 정보를 가져오지 못했습니다.");
        }

        // 사용자 정보 매핑
        OAuthUserInfo userInfo = new OAuthUserInfo();
        userInfo.setEmail(userResponse.getKakaoAccount().getEmail());
        userInfo.setName(userResponse.getProperties().getNickname());
        userInfo.setGender(userResponse.getKakaoAccount().getGender());
        userInfo.setAge(Long.valueOf(userResponse.getKakaoAccount().getAgeRange()));

        return userInfo;
    }

    /**
     * 새로운 사용자 생성
     */
    private User createNewUser(String email, String name) {
        User newUser = User.builder()
                .email(email)
                .name(name)
                .role(UserRole.USER) // 기본 역할 설정
                .build();

        return userRepository.save(newUser);
    }
}
