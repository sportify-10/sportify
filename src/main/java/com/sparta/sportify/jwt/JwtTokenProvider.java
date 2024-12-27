package com.sparta.sportify.jwt;

import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
            );

            // UserDetailsImpl로 유저 정보 반환
            return new UserDetailsImpl(user.getEmail(), role, user);

        } catch (ExpiredJwtException e) {
            throw new CustomApiException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomApiException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (MalformedJwtException e) {
            throw new CustomApiException(ErrorCode.INVALID_TOKEN);
        } catch (SignatureException e) {
            throw new CustomApiException(ErrorCode.INVALID_TOKEN_SIGNATURE);
        } catch (Exception e) {
            throw new CustomApiException(ErrorCode.TOKEN_PARSING_ERROR);
        }
    }
}
