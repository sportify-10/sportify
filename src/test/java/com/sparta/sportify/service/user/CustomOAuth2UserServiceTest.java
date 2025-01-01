package com.sparta.sportify.service.user;

import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.CustomOAuth2User;
import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest oAuth2UserRequest;
    private OAuth2User oAuth2User;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // OAuth2UserRequest와 OAuth2User 객체 설정
        oAuth2UserRequest = mock(OAuth2UserRequest.class);
        oAuth2User = mock(OAuth2User.class);

        // 테스트에 필요한 사용자 정보 설정
        attributes = new HashMap<>();
        attributes.put("email", "testuser@example.com");
        attributes.put("nickname", "Test User");
        attributes.put("id", "12345");

        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(mock(ClientRegistration.class));
        when(oAuth2UserRequest.getClientRegistration().getRegistrationId()).thenReturn("kakao");
    }

    @Test
    @DisplayName("기존 사용자가 OAuth로 로그인하면 사용자 정보를 반환해야 한다.")
    void loadUser_existingUser() {
        // UserRepository mock 설정
        User existingUser = new User();
        existingUser.setEmail("testuser@example.com");
        existingUser.setOauthId("12345");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(existingUser));

        // OAuth2UserRequest Mock 설정
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId("8b47c5604012e2fb623c2128f373574c")
                .clientSecret("eJoAZTphsgicrw160B466b6YwC8ShgOv")
                .scope("profile_nickname", "account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .redirectUri("http://localhost:8081/api/users/oAuth/login")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        // loadUser 호출
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals("Test User", result.getName()); // 이름이 "Test User"인지 확인
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
    }

    @Test
    @DisplayName("새로운 사용자가 OAuth로 로그인하면 새로운 사용자가 등록되어야 한다.")
    void loadUser_newUser() {
        // 새로운 사용자일 경우
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.empty());

        // OAuth2UserRequest Mock 설정
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId("8b47c5604012e2fb623c2128f373574c")
                .clientSecret("eJoAZTphsgicrw160B466b6YwC8ShgOv")
                .scope("profile_nickname", "account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .redirectUri("http://localhost:8081/api/users/oAuth/login")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access-token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        // loadUser 호출
        OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals("Test User", result.getName()); // 이름이 "Test User"인지 확인
        verify(userRepository, times(1)).findByEmail("testuser@example.com");
        verify(userRepository, times(1)).save(any(User.class)); // 새로운 사용자 저장 확인
    }

    @Test
    @DisplayName("공급자에 따른 nameAttributeKey 값이 카카오일 경우 'id'여야 한다.")
    void getNameAttributeKey_kakao() {
        String provider = "kakao";
        String result = customOAuth2UserService.getNameAttributeKey(provider);
        assertEquals("id", result);
    }

    @Test
    @DisplayName("공급자에 따른 nameAttributeKey 값이 구글일 경우 'sub'여야 한다.")
    void getNameAttributeKey_google() {
        String provider = "google";
        String result = customOAuth2UserService.getNameAttributeKey(provider);
        assertEquals("sub", result);
    }

    @Test
    @DisplayName("지원하지 않는 OAuth 공급자가 제공될 경우 IllegalArgumentException이 발생해야 한다.")
    void getNameAttributeKey_unsupportedProvider() {
        String provider = "unknown";
        assertThrows(IllegalArgumentException.class, () -> customOAuth2UserService.getNameAttributeKey(provider));
    }
}
