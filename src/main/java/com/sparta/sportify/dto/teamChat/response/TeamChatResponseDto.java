package com.sparta.sportify.dto.teamChat.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamChatResponseDto {
	private Long userId;
	private Long teamId;
	private String content;
}
