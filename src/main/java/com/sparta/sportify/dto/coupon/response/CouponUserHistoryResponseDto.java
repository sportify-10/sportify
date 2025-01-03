package com.sparta.sportify.dto.coupon.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CouponUserHistoryResponseDto {
    private String name;
    private String code;
    private LocalDateTime createdAt;
    private Long price;
}
