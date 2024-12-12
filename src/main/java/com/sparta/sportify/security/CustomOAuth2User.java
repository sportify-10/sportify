package com.sparta.sportify.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;

    public CustomOAuth2User(OAuth2User oAuth2User) {
        this.oAuth2User = oAuth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        // OAuth2 프로바이더에서 사용자 식별자로 사용하는 필드를 반환합니다.
        // 예를 들어, Kakao의 경우 "id"를 사용할 수 있습니다.
        return oAuth2User.getAttribute("id").toString();
    }

    public String getEmail() {
        // 이메일 정보를 반환합니다. 프로바이더마다 다를 수 있으니 확인하세요.
        return oAuth2User.getAttribute("email");
    }

    public String getNameAttribute() {
        // 사용자의 이름을 반환합니다.
        return oAuth2User.getAttribute("name");
    }
}
