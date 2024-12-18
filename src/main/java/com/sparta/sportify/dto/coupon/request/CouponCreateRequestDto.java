package com.sparta.sportify.dto.coupon.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CouponCreateRequestDto {
    String code;

    String name;

    Long count;

    LocalDate expireDate;

    Long price;
}
