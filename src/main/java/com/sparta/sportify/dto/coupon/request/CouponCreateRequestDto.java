package com.sparta.sportify.dto.coupon.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDto {
    String code;

    String name;

    Long count;

    LocalDate expireDate;

    Long price;
}
