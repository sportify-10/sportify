package com.sparta.sportify.config;

import com.sparta.sportify.jwt.JwtAuthenticationFilter;
import com.sparta.sportify.security.OAuth2LoginSuccessHandler;
import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.sparta.sportify.config.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final PasswordEncoder customPasswordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2LoginSuccessHandler successHandler;


    public SecurityConfig(@Qualifier("customPasswordEncoder") PasswordEncoder passwordEncoder, JwtAuthenticationFilter jwtAuthenticationFilter, CustomOAuth2UserService customOAuth2UserService, OAuth2LoginSuccessHandler successHandler) {
        this.customPasswordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/oAuth/login","/api/users/signup", "/api/users/login", "/api/users/oauth2/code/kakao","/login").permitAll() // 회원가입/로그인은 인증 불필요
                        .anyRequest().authenticated() // 나머지는 인증 필요

                )

                .oauth2Login(oauth2 -> oauth2
                        //.defaultSuccessUrl("/api/users/oauth/loginInfo", true)
                        .loginProcessingUrl("/api/users/oAuth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler) // 성공 핸들러 등록
                );

//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//                        })
//                );
        // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
        // JWT 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }




}
