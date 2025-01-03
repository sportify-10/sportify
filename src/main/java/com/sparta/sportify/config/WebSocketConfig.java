package com.sparta.sportify.config;

import com.sparta.sportify.config.websocket.TeamChatWebSocketHandler;
import com.sparta.sportify.config.websocket.WebSocketHandshakeInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.sparta.sportify.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	private final JwtTokenProvider jwtTokenProvider;
	private final TeamChatWebSocketHandler teamChatWebSocketHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(teamChatWebSocketHandler, "/ws/{teamId}")
			.addInterceptors(new WebSocketHandshakeInterceptor(jwtTokenProvider))
			.setAllowedOrigins("http://43.201.213.222:8080");
	}
}
