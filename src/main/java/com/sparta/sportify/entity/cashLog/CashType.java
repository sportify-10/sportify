package com.sparta.sportify.entity.cashLog;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CashType {
    REFUND("환불"),PAYMENT("사용"),CHARGE("충전"),COUPON("쿠폰사용");

    private final String value;
}
