package com.sparta.sportify.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sparta.sportify.jwt.JwtTokenProvider;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getHeaders().getFirst("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            UserDetailsImpl userDetails = jwtTokenProvider.validateToken(jwtToken);
            attributes.put("user", userDetails);
        }

        String path = request.getURI().getPath();
        String[] parts = path.split("/");

        String teamId = parts[parts.length - 1];
        attributes.put("teamId", Long.parseLong(teamId));

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // WebSocket 핸드셰이크 후 처리할 내용 (필요시)
    }
}
