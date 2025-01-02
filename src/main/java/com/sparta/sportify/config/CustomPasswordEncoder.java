package com.sparta.sportify.config;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class CustomPasswordEncoder implements PasswordEncoder {

    private static final int BCRYPT_COST = 12; // 적절한 비용 설정

    @Override
    public String encode(CharSequence rawPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, rawPassword.toString().toCharArray());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toString().toCharArray(), encodedPassword);
        return result.verified;
    }
}

