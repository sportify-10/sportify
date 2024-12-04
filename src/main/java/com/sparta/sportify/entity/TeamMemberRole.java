package com.sparta.sportify.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TeamMemberRole {
    USER("유저"),MANAGER("관리자");

    private final String value;
}
