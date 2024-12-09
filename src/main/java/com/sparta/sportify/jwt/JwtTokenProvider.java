package com.sparta.sportify.jwt;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret.key}") // application.yml에서 값을 가져옴
    private String secretKey; // JWT 비밀 키

    // JWT 토큰 검증 및 유저 정보 반환
    public UserDetailsImpl validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // JWT 토큰에서 유저 정보 추출 (예: username, role)
            String name = claims.getSubject();
            String roleString = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleString); // role을 UserRole enum으로 변환

            // User 객체를 토큰에서 직접 추출하는 것보다, UserRepository를 통해 DB에서 가져오는 것이 일반적
            User user = new User();  // 예시로 비어있는 User 객체 생성 (여기선 데이터베이스에서 조회해야 함)

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

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getName())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1일 유효
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


}
