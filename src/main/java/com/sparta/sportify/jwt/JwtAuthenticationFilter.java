package com.sparta.sportify.jwt;

import com.sparta.sportify.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.rmi.RemoteException;

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

        // Authorization 헤더에서 Bearer 토큰을 추출
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
}
