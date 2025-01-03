package com.sparta.sportify.dto.user.res;


import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String token;

    @Builder
    public LoginResponseDto(String token) {
        this.token = token;
    }
}

