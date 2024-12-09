package com.sparta.sportify.dto.user.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class OAuthResponseDto {
    private String accessToken; // JWT Access Token
    private String email;       // 사용자 이메일
    private String name;        // 사용자 이름
//    private String provider;    // OAuth 제공자 정보 (Kakao, Naver 등)
//    private boolean isNewUser;  // 신규 가입 여부
}
