package com.sparta.sportify.service.oauth;

import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // DefaultOAuth2UserService의 loadUser 메서드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 클라이언트 registrationId 확인 (예: kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 플랫폼별 사용자 정보 파싱
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(registrationId, attributes);
        String nickname = extractNickname(registrationId, attributes);
        String oauthId = extractOauthId(registrationId, attributes);

        // 사용자 정보를 기반으로 회원가입 처리
        return processUserRegistration(email, nickname, oauthId, registrationId, oAuth2User);
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }
        // 다른 플랫폼의 이메일 추출 로직 추가 가능
        throw new IllegalArgumentException("Unsupported platform: " + registrationId);
    }

    private String extractNickname(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            return (String) profile.get("nickname");
        }
        // 다른 플랫폼의 닉네임 추출 로직 추가 가능
        throw new IllegalArgumentException("Unsupported platform: " + registrationId);
    }

    private String extractOauthId(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return String.valueOf(attributes.get("id"));
        }
        // 다른 플랫폼의 OAuth ID 추출 로직 추가 가능
        throw new IllegalArgumentException("Unsupported platform: " + registrationId);
    }

    private OAuth2User processUserRegistration(String email, String nickname, String oauthId, String registrationId, OAuth2User oAuth2User) {
        // 이미 존재하는 사용자 확인
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return new DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    oAuth2User.getAttributes(),
                    "email"
            );
        }

        // 새로운 사용자 등록
        User newUser = User.builder()
                .email(email)
                .name(nickname)
                .oauthId(oauthId)
                .oauthProvider(registrationId)
                .role(UserRole.USER) // 기본 ROLE_USER로 설정
                .active(true) // 활성화 여부
                .build();

        userRepository.save(newUser);

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}