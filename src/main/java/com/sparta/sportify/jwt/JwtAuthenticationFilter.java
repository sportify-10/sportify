package com.sparta.sportify.jwt;

import com.sparta.sportify.exception.CustomValidationException;
import com.sparta.sportify.security.UserDetailsImpl;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.rmi.RemoteException;

import com.sparta.sportify.jwt.JwtUtil;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // JWT 토큰 가져오기
        String token = jwtUtil.resolveToken(request);
        if (token != null && jwtUtil.validateToken(token)!=null) {
            String username = jwtUtil.getUsernameFromToken(token);  // 토큰에서 username 가져오기
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);  // email 대신 username으로 처리

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 인증 정보를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 삭제된 사용자 확인
            if (((UserDetailsImpl) userDetails).getUser().getDeletedAt() != null) {
                throw new RemoteException("삭제된 유저입니다.");  // 삭제된 사용자 예외 처리

            }
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    // 요청에서 토큰을 가져오는 메서드
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거하고 토큰 반환
        }
        return null;
    }
}
