package com.sparta.sportify.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CashType {
    REFUND("환불"),APPOROVE("승인");

    private final String value;
}
