package com.sparta.sportify.dto.kakaoPay.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoPayRefundRequestDto {
    private String tid;
    private Long amount;
}
