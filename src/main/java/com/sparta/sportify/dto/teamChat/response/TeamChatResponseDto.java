package com.sparta.sportify.dto.teamChat.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;

@Getter
public class TeamChatResponseDto {
	private Long userId;
	private Long teamId;
	private String content;

	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private String timestamp;

	public TeamChatResponseDto(Long userId, Long teamId, String content, LocalDateTime timestamp) {
		this.userId = userId;
		this.teamId = teamId;
		this.content = content;
		//this.timestamp = timestamp;
		this.timestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
	}
}
