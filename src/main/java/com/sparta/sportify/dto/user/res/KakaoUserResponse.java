package com.sparta.sportify.dto.user.res;

import lombok.Data;

@Data
public class KakaoUserResponse {
    private Long id;
    private KakaoAccount kakaoAccount;
    private Properties properties;

    @Data
    public static class KakaoAccount {
        private String email;
        private String gender;
        private String ageRange;
    }

    @Data
    public static class Properties {
        private String nickname;
    }
}
