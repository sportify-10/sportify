package com.sparta.sportify.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MatchStatus {
    CLOSED("마감"),PROGRESS("진행중");

    private final String value;
}
