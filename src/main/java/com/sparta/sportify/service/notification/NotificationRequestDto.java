package com.sparta.sportify.service.notification;

import lombok.Data;

@Data
public class NotificationRequestDto {
    private String stadiumName;
    private String startTime; // ISO-8601 형식 (예: "2024-12-26T15:00:00")
}
