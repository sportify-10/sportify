package com.sparta.sportify.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {

    USER("유저"),ADMIN("관리자");

    private final String value;
}
