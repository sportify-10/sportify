package com.sparta.sportify.service.oauth;

import com.sparta.sportify.config.PasswordEncoder;
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
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // DefaultOAuth2UserService의 loadUser 메서드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 클라이언트 registrationId 확인 (예: kakao, naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 플랫폼별 사용자 정보 파싱
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractUserAttributes(oAuth2User);

        // 사용자 정보를 기반으로 회원가입 처리
        return processUserRegistration(oAuth2User);
    }

    public String extractUserAttributes(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return (String) kakaoAccount.get("email");
    }

    private OAuth2User processUserRegistration(OAuth2User oAuth2User) {
        String email = extractUserAttributes(oAuth2User);
        String randomPassword = UUID.randomUUID().toString(); // 임의 비밀번호 생성
        String encodedPassword = passwordEncoder.encode(randomPassword); // 암호화
        // 이미 존재하는 사용자 확인
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            // 새로운 사용자 등록
            UserRequestDto requestDto = new UserRequestDto();
            requestDto.setEmail(email);
            requestDto.setPassword(encodedPassword);
            requestDto.setRole(UserRole.USER); // 기본 ROLE_USER로 설정
            User newUser = userRepository.save(new User(requestDto));
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "email");
        }

        // 이미 존재하는 사용자라면, 그 사용자 반환
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "email");
    }
}