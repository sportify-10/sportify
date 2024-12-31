package com.sparta.sportify.jwt;

import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}") // application.yml에서 값을 가져옴
    private String secretKey;

    @Value("${jwt.secret.expiration-time}") // application.yml에서 값을 가져옴
    private long expirationTime;

    // JWT 토큰 생성
    public String generateToken(String email, UserRole role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰 검증 및 유저 정보 반환
    public UserDetailsImpl validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String name = claims.getSubject();
            String roleString = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleString);

            return new UserDetailsImpl(name, role, null);


        } catch (SignatureException e) {
            throw new CustomApiException(ErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            throw new CustomApiException(ErrorCode.TOKEN_PARSING_ERROR);
        }
    }

    // 요청에서 토큰을 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거 후 토큰 반환
        }
        return null;
    }
}
