package com.sparta.sportify.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.util.Collection;
import java.util.List;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {
    private final String provider; // ex: kakao, naver
    private final String providerId; // 플랫폼별 사용자 ID
    private final String email;
    private final String nickname;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(String provider, String providerId, String email, String nickname, Map<String, Object> attributes) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.attributes = attributes;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return providerId;
    }
}
