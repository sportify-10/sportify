package com.sparta.sportify.entity.notification;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Setter
    @Getter
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String message;
    private String type; // 예: "MATCH"
    private String deliveryMethod; // 예: "PUSH"
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status; // 상태 (PENDING, SENT 등)

    @Builder
    public Notification(Long userId, String message, String type, String deliveryMethod, LocalDateTime createdAt, NotificationStatus status) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.deliveryMethod = deliveryMethod;
        this.createdAt = createdAt;
        this.status = status;
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
}
