package com.sparta.sportify.controller.TeamChat;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamChat.TeamChatService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class TeamChatController {
	private final TeamChatService teamChatService;

	@PostMapping("/join/{teamId}")
	public ResponseEntity<ApiResult<String>> joinTeamChatting(
		@PathVariable Long teamId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		teamChatService.joinTeamChatting(teamId, userDetails);
		String webSocketUrl = "ws://localhost:8080/ws/" + teamId;
		return ResponseEntity.ok(ApiResult.success("팀 채팅 참가 성공", webSocketUrl));
	}

	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResult<List<TeamChatResponseDto>>> getChatData(
		@PathVariable Long teamId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("팀 채팅 내역 조회 성공", teamChatService.getChatData(teamId, userDetails)));
	}
}
