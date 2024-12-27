package com.sparta.sportify.service.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private String message;
    private LocalDateTime timestamp;
}