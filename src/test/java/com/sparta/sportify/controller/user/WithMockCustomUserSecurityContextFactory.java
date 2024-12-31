package com.sparta.sportify.controller.user;

import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser withMockCustomUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add((GrantedAuthority) () -> "ROLE_ADMIN");
        User user = User.builder()
                .email(withMockCustomUser.username())
                .age(30L)
                .cash(30000L)
                .name(withMockCustomUser.name())
                .gender("male")
                .levelPoints(1000L)
                .password("Password123!")
                .role(UserRole.USER)
                .region("Seoul")
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getEmail(), user.getRole(), user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, "testPassword", grantedAuthorities);
        context.setAuthentication(authentication);
        return context;
    }
}