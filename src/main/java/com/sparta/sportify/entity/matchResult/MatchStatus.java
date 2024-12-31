package com.sparta.sportify.entity.matchResult;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MatchStatus {
    CLOSED("마감"),
    ALMOST_FULL("마감 임박"),
    OPEN("모집 중");

    private final String value;
}
