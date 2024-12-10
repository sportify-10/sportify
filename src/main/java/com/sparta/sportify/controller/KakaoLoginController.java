package com.sparta.sportify.controller;

import com.sparta.sportify.service.oauth.KakaoLoginService;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("kakaoUrl", kakaoLoginService.getKakaoLogin());

        return "model";
    }

}
