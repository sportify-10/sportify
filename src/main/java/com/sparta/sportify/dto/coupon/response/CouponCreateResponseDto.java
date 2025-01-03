package com.sparta.sportify.dto.coupon.response;

import com.sparta.sportify.entity.coupon.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateResponseDto {
    Long id;
    String code;
    String name;
    Long count;
    LocalDate expireDate;
    Long price;

    public CouponCreateResponseDto(Coupon coupon) {
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.name = coupon.getName();
        this.count = coupon.getCount();
        this.expireDate = coupon.getExpireDate();
        this.price = coupon.getPrice();
    }

}
