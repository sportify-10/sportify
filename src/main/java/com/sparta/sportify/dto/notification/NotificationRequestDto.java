package com.sparta.sportify.dto.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequestDto {
    private Long userId;        // 사용자 ID
    private String stadiumName; // 경기장 이름
    private String startTime;   // 시작 시간
}
