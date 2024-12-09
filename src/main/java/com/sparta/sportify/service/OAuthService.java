package com.sparta.sportify.service;

import com.sparta.sportify.dto.user.req.OAuthLoginRequestDto;
import com.sparta.sportify.dto.user.req.OAuthUserInfo;
import com.sparta.sportify.dto.user.res.OAuthResponseDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.service.oauth.KakaoOAuthService;
import com.sparta.sportify.service.oauth.NaverOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NaverOAuthService naverOAuthService;
    private final KakaoOAuthService kakaoOAuthService;

    /**
     * OAuth 로그인 처리
     */
    public OAuthResponseDto oauthLogin(OAuthLoginRequestDto requestDto) {
        OAuthUserInfo userInfo;

        // OAuth 제공자 및 Access Token 검증
        switch (requestDto.getProvider().toLowerCase()) {
            case "kakao":
                userInfo = kakaoOAuthService.getUserInfo(requestDto.getAccessToken());
                break;
            case "naver":
                userInfo = naverOAuthService.getUserInfo(requestDto.getAccessToken());
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 OAuth 제공자입니다.");
        }

        if (userInfo == null) {
            throw new IllegalArgumentException("OAuth 인증 실패: 유효하지 않은 Access Token입니다.");
        }

        // 이메일을 통해 사용자 검색
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 처리
            user = existingUser.get();
        } else {
            // 새로운 사용자 생성
            user = createNewUser(userInfo);
        }

        // JWT 토큰 발급
        String token = jwtTokenProvider.generateToken(user);

        return new OAuthResponseDto(user.getName(), user.getEmail(), token);
    }

    /**
     * 새로운 사용자 생성
     */
    private User createNewUser(OAuthUserInfo userInfo) {
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .role(UserRole.USER) // 기본 역할 USER
                .region(userInfo.getRegion())
                .age(userInfo.getAge())
                .gender(userInfo.getGender())
                .build();

        return userRepository.save(newUser);
    }
}
