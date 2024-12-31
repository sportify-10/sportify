package com.sparta.sportify.controller.teamChat;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamChatService;
import com.sparta.sportify.util.api.ApiResult;

class TeamChatControllerTest {

	@InjectMocks
	private TeamChatController teamChatController;

	@Mock
	private TeamChatService teamChatService;

	private UserDetailsImpl userDetails;
	private User user;
	private Long teamId = 1L;
	private TeamChatResponseDto teamChatResponseDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = User.builder()
			.id(1L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L)
			.build();
		userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);

		LocalDateTime timestamp = LocalDateTime.of(2024, 12, 30, 16, 44, 0);
		teamChatResponseDto = new TeamChatResponseDto(user.getId(), teamId, "내용", timestamp);
	}

	@Test
	void joinTeamChatting() {
		String expectedWebSocketUrl = "ws://localhost:8080/ws/" + teamId;

		// Act
		ResponseEntity<ApiResult<String>> response = teamChatController.joinTeamChatting(teamId, userDetails);

		// Assert
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("팀 채팅 참가 성공");
		assertThat(response.getBody().getData()).isEqualTo(expectedWebSocketUrl);
	}

	@Test
	void getChatData() {
		List<TeamChatResponseDto> chatData = Arrays.asList(teamChatResponseDto);
		when(teamChatService.getChatData(anyLong(), any())).thenReturn(chatData);

		ResponseEntity<ApiResult<List<TeamChatResponseDto>>> response = teamChatController.getChatData(teamId,
			userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("팀 채팅 내역 조회 성공");
		assertThat(response.getBody().getData().get(0).getContent()).isEqualTo("내용");
	}
}