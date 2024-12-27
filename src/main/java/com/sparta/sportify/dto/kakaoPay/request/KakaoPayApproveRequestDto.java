package com.sparta.sportify.dto.kakaoPay.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveRequestDto {
    private String tid;
    private String pgToken;
    private String userId;
    private Long amount;
}
