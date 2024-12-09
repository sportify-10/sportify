package com.sparta.sportify.dto.user.res;

import lombok.Data;

@Data
public class NaverUserResponse {
    private String resultcode;
    private String message;
    private Response response;

    @Data
    public static class Response {
        private String email;
        private String name;
        private String gender;
        private String age;
    }
}
