package com.sparta.sportify.service.notification;


import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificationPayload(@JsonProperty("userId") Long userId,
                                  @JsonProperty("message") String message) {
    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public NotificationPayload(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }
}
