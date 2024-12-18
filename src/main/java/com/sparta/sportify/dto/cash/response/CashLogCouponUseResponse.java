package com.sparta.sportify.dto.cash.response;

import com.sparta.sportify.entity.CashType;
import com.sparta.sportify.entity.Coupon;
import com.sparta.sportify.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CashLogCouponUseResponse {
    Long price;

    LocalDateTime createAt;

    CashType type;

    String couponCode;
}
