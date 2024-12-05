package com.sparta.sportify.jwt;

import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "ozeoPDLdtIuii8Vt7SxsVZJiK8wQnYRwxZpMZErKX9Y="; // JWT 서명 키
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    // JWT 토큰 생성
    public String generateToken(String email) {
        return "Bearer " + Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // JWT 토큰 검증 및 유저 정보 반환
    public UserDetailsImpl validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            // JWT 토큰에서 유저 정보 추출 (예: username, role)
            String username = claims.getSubject();
            String role = (String) claims.get("role");

            // UserDetailsImpl로 유저 정보 반환
            return new UserDetailsImpl(username, role);

        } catch (SignatureException e) {
            // 토큰 서명 오류
            throw new IllegalArgumentException("Invalid token");
        } catch (Exception e) {
            // 기타 오류
            throw new IllegalArgumentException("Token parsing error");
        }
    }
}
