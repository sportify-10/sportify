package com.sparta.sportify.service.oauth;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 플랫폼별 사용자 정보 추출
        String email = extractEmail(attributes, registrationId);
        String oauthId = extractOauthId(attributes, registrationId);
        String nickname = extractNickname(attributes, registrationId);

        // 사용자 등록 또는 업데이트 처리
        User user = processUserRegistration(email, oauthId, registrationId, nickname);

        return oAuth2User;
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                    return (String) kakaoAccount.get("email");
                }
                break;
            case "google":
                return (String) attributes.get("email");
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                if (naverResponse != null) {
                    return (String) naverResponse.get("email");
                }
                break;
        }
        return null;
    }

    private String extractOauthId(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "kakao":
                return String.valueOf(attributes.get("id"));
            case "google":
                return (String) attributes.get("sub");
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                if (naverResponse != null) {
                    return (String) naverResponse.get("id");
                }
                break;
        }
        return null;
    }

    private String extractNickname(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                if (properties != null && properties.containsKey("nickname")) {
                    return (String) properties.get("nickname");
                }
                break;
            case "google":
                return (String) attributes.get("name");
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                if (naverResponse != null) {
                    return (String) naverResponse.get("nickname");
                }
                break;
        }
        return "anonymous"; // 기본값
    }

    private User processUserRegistration(String email, String oauthId, String registrationId, String nickname) {
        Optional<User> existingUser;

        if (email == null || email.isEmpty()) {
            existingUser = userRepository.findByOauthId(oauthId);
            if (existingUser.isEmpty()) {
                email = "anonymous-" + oauthId + "@example.com";
            }
        } else {
            existingUser = userRepository.findByEmail(email);
        }

        if (existingUser.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setOauthId(oauthId);
            user.setOauthProvider(registrationId.toLowerCase());
            user.setName(nickname);
            user.setPassword(UUID.randomUUID().toString()); // 임시 비밀번호
            user.setRole(UserRole.USER);

            return userRepository.save(user);
        } else {
            User user = existingUser.get();
            user.setName(nickname);
            return userRepository.save(user);
        }
    }


    public String extractUserAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = (String) attributes.get("registration_id");

        switch (registrationId) {
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                    return (String) kakaoAccount.get("email");
                }
                break;
            case "google":
                if (attributes.containsKey("email")) {
                    return (String) attributes.get("email");
                }
                break;
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                if (naverResponse != null && naverResponse.containsKey("email")) {
                    return (String) naverResponse.get("email");
                }
                break;
        }

        return null; // 이메일 없음
    }

}