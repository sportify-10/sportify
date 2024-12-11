package com.sparta.sportify.service.oauth;

import com.sparta.sportify.config.PasswordEncoder;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        logger.info("Extracted Email: {}", email);
        logger.info("Extracted OAuth ID: {}", oauthId);

        // 사용자 등록 또는 로그인 처리
        return processUserRegistration(email, oauthId, attributes, oAuth2User);
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

    private OAuth2User processUserRegistration(String email, String oauthId, Map<String, Object> attributes, OAuth2User oAuth2User) {
        Optional<User> existingUser;

        if (email == null || email.isEmpty()) {
            existingUser = userRepository.findByOauthId(oauthId);
            if (existingUser.isEmpty()) {
                // 이메일 없이 사용자를 생성
                logger.warn("Email is null or empty for OAuth ID: {}", oauthId);
                email = "anonymous-" + oauthId + "@example.com"; // 대체 이메일 생성
            }
        } else {
            existingUser = userRepository.findByEmail(email);
        }

        // 사용자 등록
        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setOauthId(oauthId);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole(UserRole.USER);

            userRepository.save(newUser);
        }

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                email != null ? "email" : "oauthId" // Principal key 설정
        );
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

}
