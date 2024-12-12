package com.sparta.sportify.dto.user.req;

import com.sparta.sportify.dto.user.req.OAuthAttributes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthLoginRequestDto {
    private String accessToken;
    private String email;
    private String provider;
    private String name;
    private String region;
    private Long age;
    private String gender;


}
