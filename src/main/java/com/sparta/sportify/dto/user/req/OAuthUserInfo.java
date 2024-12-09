package com.sparta.sportify.dto.user.req;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthUserInfo {
    private String email; // 이메일
    private String name; // 이름
    private String nickname; // 닉네임
    private String birthday; // 생년월일
    private String region;
    private String gender;
    private Long age;
    // Getter and Setter
}
