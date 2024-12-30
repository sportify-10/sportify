package com.sparta.sportify.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;            // 알림 ID
    private String message;     // 알림 메시지
    private LocalDateTime createdAt; // 생성 시간
}
