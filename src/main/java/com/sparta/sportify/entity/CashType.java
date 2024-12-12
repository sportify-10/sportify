package com.sparta.sportify.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CashType {
    REFUND("환불"),PAYMENT("사용"),CHARGE("충전");

    private final String value;
}
