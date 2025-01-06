package com.sparta.sportify.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.jwt.JwtAuthenticationFilter;
import com.sparta.sportify.security.OAuth2LoginSuccessHandler;
import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
import com.sparta.sportify.util.api.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomPasswordEncoder customPasswordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2LoginSuccessHandler successHandler;

    public SecurityConfig(@Qualifier("customPasswordEncoder") CustomPasswordEncoder customPasswordEncoder, JwtAuthenticationFilter jwtAuthenticationFilter, CustomOAuth2UserService customOAuth2UserService, OAuth2LoginSuccessHandler successHandler) {
        this.customPasswordEncoder = customPasswordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/oAuth/login",
                                "/api/users/kakao/login", "/api/users/naver/login", "/api/users/google/login",
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/users/oauth2/code/kakao",
                                "/login",
                                "/api/matches/notifications",
                                "/resources/template/index.html",
                                "/sse/73",
                                "/send/73",
                                "/sendToAll",
                                "/v1/sse/subscribe",
                                "/v1/sse/broadcast")
                        .permitAll() // 회원가입/로그인은 인증 불필요
                        .anyRequest()
                        .authenticated() // 나머지는 인증 필요

                )

                .oauth2Login(oauth2 -> oauth2
                        .loginProcessingUrl("/api/users/oAuth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler) // 성공 핸들러 등록
                )
                .exceptionHandling(exception -> exception
                                .authenticationEntryPoint((request, response, authException) -> {
//                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                                    // ApiResult 형식으로 에러 메시지 생성
//                                    ApiResult<?> errorResult = ApiResult.error(401, "인증x");
                                    ApiResult<?> errorResult = ApiResult.error(401, "인증x", 401);


                                    // ObjectMapper로 객체를 JSON으로 변환하여 응답에 작성
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    String a = "{\n" +
                                            "    \"success\": false,\n" +
                                            "    \"message\": \"인증되지 않은 사용자입니다.\",\n" +
                                            "    \"data\": null,\n" +
                                            "    \"apiError\": {\n" +
                                            "        \"msg\": \"인증되지 않은 사용자입니다.\",\n" +
                                            "        \"status\": 401\n" +
                                            "    }\n" +
                                            "}";

                                    response.getWriter().write(a);
                                })
                );
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
