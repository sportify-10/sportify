package com.sparta.sportify.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtTokenProvider {

    //@Value("${JWT_SECRET_KEY:default-Secret}") // 환경 변수 사용, 기본값 설정
    @Value("${jwt.secret.key}") // '-' 문자가 Base64 인코딩 형식에 맞지 않아 사용x
    private String secretKey; // JWT 비밀 키

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // JWT 토큰 검증 및 유저 정보 반환
    public UserDetailsImpl validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            // JWT 토큰에서 이메일과 역할 정보 추출
            String email = claims.getSubject();
            String roleString = claims.get("role", String.class);
            UserRole role = UserRole.valueOf(roleString);

            // 데이터베이스에서 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            // UserDetailsImpl로 유저 정보 반환
            return new UserDetailsImpl(user.getEmail(), role, user);

        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            throw new IllegalArgumentException("Invalid JWT structure", e);
        } catch (SignatureException e) {
            throw new IllegalArgumentException("Invalid token signature", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token parsing error", e);
        }
    }
}
