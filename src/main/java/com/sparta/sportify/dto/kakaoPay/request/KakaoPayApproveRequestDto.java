package com.sparta.sportify.dto.kakaoPay.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoPayApproveRequestDto {
    private String tid;
    private String pgToken;
    private String userId;
    private Long amount;
}
