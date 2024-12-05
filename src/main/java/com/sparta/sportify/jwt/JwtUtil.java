package com.sparta.sportify.jwt;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
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

    // 토큰에서 사용자 이름 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰 검증 및 유저 정보 반환
    public UserDetailsImpl validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            // JWT 토큰에서 유저 정보 추출 (예: name, role)
            String name = claims.getSubject(); // 이메일을 name으로 사용
            String roleString = claims.get("role", String.class); // 역할을 문자열로 받음
            UserRole role = UserRole.valueOf(roleString); // 역할을 UserRole로 변환

            // 유저 정보를 DB에서 조회하는 로직이 필요함 (email로 User 객체를 찾기)
            User user = new User();  // 여기에 실제 DB에서 유저를 찾아야 함
            user.setEmail(name);
            user.setRole(role);

            // UserDetailsImpl로 유저 정보 반환
            return new UserDetailsImpl(name, role, user);

        } catch (SignatureException e) {
            // 토큰 서명 오류
            throw new IllegalArgumentException("Invalid token");
        } catch (Exception e) {
            // 기타 오류
            throw new IllegalArgumentException("Token parsing error");
        }
    }

    // 요청에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거 후 토큰 반환
        }
        return null;
    }
}
