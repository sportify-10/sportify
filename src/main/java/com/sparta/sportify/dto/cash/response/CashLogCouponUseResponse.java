package com.sparta.sportify.dto.cash.response;

import com.sparta.sportify.entity.cashLog.CashType;
import lombok.*;

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
