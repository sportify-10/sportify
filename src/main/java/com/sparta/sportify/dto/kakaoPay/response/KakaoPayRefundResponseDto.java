package com.sparta.sportify.dto.kakaoPay.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoPayRefundResponseDto {
    private String tid;
    private String cid;
    private String status;
    private String partner_order_id;
    private String partner_user_id;
    private String payment_method_type;
    private String item_name;
}
