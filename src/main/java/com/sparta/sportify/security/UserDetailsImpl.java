package com.sparta.sportify.security;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class UserDetailsImpl implements UserDetails {
    private String name;
    private UserRole role;
    private User user;

    public UserDetailsImpl(String name, UserRole role, User user) {
        this.name = name;
        this.role = role;
        this.user = user;
    }


    @Override
    public Collection<? extends SimpleGrantedAuthority> getAuthorities() {
        // ROLE_ 접두어를 추가하여 권한을 반환
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return null; // 비밀번호는 JWT에 포함되지 않으므로 null로 반환
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }



}
