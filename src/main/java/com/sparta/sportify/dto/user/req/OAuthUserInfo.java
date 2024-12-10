package com.sparta.sportify.dto.user.req;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthUserInfo {
    private String email;
    private String name;

}

