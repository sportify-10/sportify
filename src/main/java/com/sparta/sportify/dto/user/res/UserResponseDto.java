//package com.sparta.sportify.dto.user.res;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@RequiredArgsConstructor
//@Getter
//public class UserResponseDto {
//
//    private final boolean success;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
//    private final LocalDateTime timeStamp;
//
//    private final Object data;
//
//    @Builder
//    public UserResponseDto(boolean success, Object data) {
//        this.success = success;
//        this.timeStamp = LocalDateTime.now(); // 현재 시간 자동 생성
//        this.data = data;
//    }
//
//    public static UserResponseDto ofSuccess(Object data) {
//        return UserResponseDto.builder()
//                .success(true)
//                .data(data)
//                .build();
//    }
//
//    public static UserResponseDto ofFailure() {
//        return UserResponseDto.builder()
//                .success(false)
//                .data(null)
//                .build();
//    }
//}
