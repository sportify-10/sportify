package com.sparta.sportify.service.oauth;

import com.sparta.sportify.config.PasswordEncoder;
import com.sparta.sportify.dto.user.req.OAuthAttributes;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;// JWT 생성기 주입

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // DefaultOAuth2UserService의 loadUser 메서드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 클라이언트 registrationId 확인 (예: kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 플랫폼별 사용자 정보 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = extractEmail(attributes, registrationId);
        String oauthId = extractOauthId(attributes);
        String nickname = extractNickname(attributes, registrationId); // 닉네임 추출

        logger.info("Extracted Email: {}", email);
        logger.info("Extracted OAuth ID: {}", oauthId);
        logger.info("Extracted Nickname: {}", nickname);

        // 사용자 등록 또는 업데이트 처리
        User user = processUserRegistration(email, oauthId, registrationId, nickname);

        // 사용자와 속성을 포함하는 OAuth2User 반환
        return oAuth2User;
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                return (String) kakaoAccount.get("email");
            }
        }
        return (String) attributes.getOrDefault("email", null); // 다른 플랫폼은 기본 이메일 필드 확인
    }

    private String extractOauthId(Map<String, Object> attributes) {
        return String.valueOf(attributes.get("id"));
    }

    private String extractNickname(Map<String, Object> attributes, String registrationId) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            if (properties != null && properties.containsKey("nickname")) {
                return (String) properties.get("nickname");
            }
        }
        return "anonymous"; // 닉네임 기본값
    }

    private User processUserRegistration(String email, String oauthId, String registrationId, String nickname) {
        Optional<User> existingUser;

        if (email == null || email.isEmpty()) {
            existingUser = userRepository.findByOauthId(oauthId);
            if (existingUser.isEmpty()) {
                logger.warn("Email is null or empty for OAuth ID: {}", oauthId);
                email = "anonymous-" + oauthId + "@example.com";
            }
        } else {
            existingUser = userRepository.findByEmail(email);
        }

        if (existingUser.isEmpty()) {
            // 새로운 사용자 등록
            User user = new User();
            user.setEmail(email);
            user.setOauthId(oauthId);
            user.setOauthProvider(registrationId.toLowerCase()); // OAuth 제공자 저장
            user.setName(nickname); // 닉네임 저장
            user.setPassword(UUID.randomUUID().toString()); // 임시 비밀번호
            user.setRole(UserRole.USER);

            return userRepository.save(user);
        } else {
            // 기존 사용자 업데이트 (필요 시 닉네임 변경 가능)
            User user = existingUser.get();
            user.setName(nickname); // 닉네임 업데이트
            return userRepository.save(user);
        }
    }
    public String extractUserAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("OAuth2User attributes: {}", attributes);

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
            logger.warn("Email not found in Kakao account attributes");
            return null; // 이메일 없음
        }

        return kakaoAccount.get("email").toString(); // 이메일 반환
    }
    public String extractNaverUserAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("OAuth2User attributes: {}", attributes);

        // 네이버 로그인 시 사용자 정보 구조
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        if (response == null || !response.containsKey("email")) {
            logger.warn("Email not found in Naver account attributes");
            return null; // 이메일 없음
        }

        return response.get("email").toString(); // 이메일 반환
    }
    public String extractGoogleUserAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("OAuth2User attributes: {}", attributes);

        if (!attributes.containsKey("email")) {
            logger.warn("Email not found in Google account attributes");
            return null; // 이메일 없음
        }

        return attributes.get("email").toString(); // 이메일 반환
    }

}