package com.sparta.sportify.service.oauth;

import com.sparta.sportify.dto.user.res.OAuthResponseDto;

public interface OAuthService {
    OAuthResponseDto login(String accessToken);
}
