package com.sparta.sportify.dto.coupon.response;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class CouponUserHistoryResponseDto {
    private String name;
    private String code;
    private LocalDateTime createdAt;
    private Long price;
}
