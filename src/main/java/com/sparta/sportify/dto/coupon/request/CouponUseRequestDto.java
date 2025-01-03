package com.sparta.sportify.dto.coupon.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponUseRequestDto {
    @NotBlank
    String code;
}
