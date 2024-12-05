package com.sparta.sportify.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.sparta.sportify.config.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final PasswordEncoder customPasswordEncoder;

    public SecurityConfig(@Qualifier("customPasswordEncoder") PasswordEncoder passwordEncoder) {
        this.customPasswordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll() // 회원가입/로그인은 인증 불필요
                        .anyRequest().authenticated() // 나머지는 인증 필요
                );

        return http.build();
    }
}

