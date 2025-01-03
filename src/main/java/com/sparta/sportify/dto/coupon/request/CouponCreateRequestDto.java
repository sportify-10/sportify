package com.sparta.sportify.dto.coupon.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDto {
    @NotBlank
    String code;

    @NotBlank
    String name;

    @NotBlank
    Long count;

    @NotBlank
    LocalDate expireDate;

    @NotBlank
    Long price;
}
