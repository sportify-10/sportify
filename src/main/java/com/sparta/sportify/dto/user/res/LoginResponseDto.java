package com.sparta.sportify.dto.user.res;


import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final boolean success;
    private final String timeStamp;
    private final String token;

    @Builder
    public LoginResponseDto(boolean success, String timeStamp, String token) {
        this.success = success;
        this.timeStamp = timeStamp;
        this.token = token;
    }
}

