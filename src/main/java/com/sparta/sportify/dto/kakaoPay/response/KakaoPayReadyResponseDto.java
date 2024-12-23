package com.sparta.sportify.dto.kakaoPay.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoPayReadyResponseDto {
    private String tid; // 결제 고유번호
    private String nextRedirectUrl; // 결제 페이지로 리디렉션할 URL
}