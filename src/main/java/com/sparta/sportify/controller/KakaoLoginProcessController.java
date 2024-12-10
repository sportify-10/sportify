package com.sparta.sportify.controller;

import com.sparta.sportify.dto.user.req.KakaoDTO;
import com.sparta.sportify.service.oauth.KakaoLoginService;
import com.sparta.sportify.util.api.MsgEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class KakaoLoginProcessController {
    private final KakaoLoginService kakaoLoginService;

    @GetMapping("/oauth2/code/kakao")
    public ResponseEntity<MsgEntity> callback(HttpServletRequest request) throws Exception{

        KakaoDTO kakaoInfo = kakaoLoginService.getKakaoInfo(request.getParameter("code"));

        return ResponseEntity.ok()
                .body(new MsgEntity("success", kakaoInfo));
    }
}
