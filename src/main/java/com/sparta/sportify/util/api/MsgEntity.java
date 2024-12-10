package com.sparta.sportify.util.api;

import com.sparta.sportify.dto.user.req.KakaoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MsgEntity {
    String message;
    KakaoDTO kakaoDTO;
}
