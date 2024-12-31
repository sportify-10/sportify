package com.sparta.sportify.controller.team;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sparta.sportify.dto.teamDto.req.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.res.DeleteResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponsePage;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamService;
import com.sparta.sportify.util.api.ApiResult;

class TeamControllerTest {

	@InjectMocks
	private TeamController teamController;

	@Mock
	private TeamService teamService;

	private User user;
	private UserDetailsImpl userDetails;
	private TeamRequestDto teamRequestDto;
	private TeamResponseDto teamResponseDto;
	private Team team;
	private DeleteResponseDto deleteResponseDto;

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

		teamRequestDto = new TeamRequestDto(
			"팀 이름",
			"지역",
			"20-22",
			"초보",
			"축구",
			"설명");

		teamResponseDto = new TeamResponseDto(
			1L, "A팀", "지역", "20-22", "초보", "축구", "설명", 1000
		);

	}

	@Test
	void createTeam() {
		when(teamService.createTeam(eq(teamRequestDto), eq(userDetails.getUser().getId())))
			.thenReturn(teamResponseDto);

		ResponseEntity<ApiResult<TeamResponseDto>> response = teamController.createTeam(teamRequestDto, userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("팀 생성 완료");
		assertThat(response.getBody().getData()).isEqualTo(teamResponseDto);

		verify(teamService, times(1)).createTeam(eq(teamRequestDto), eq(userDetails.getUser().getId()));
	}

	@Test
	void getAllTeams() {
		TeamResponsePage teamResponsePage = new TeamResponsePage(
			List.of(teamResponseDto), 1, 10
		);

		when(teamService.getAllTeams(1, 10, null, null, null))
			.thenReturn(teamResponsePage);

		ResponseEntity<ApiResult<TeamResponsePage>> response = teamController.getAllTeams(0, 10, null, null, null);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMessage()).isEqualTo("팀 전체 조회 완료");
		// assertThat(response.getBody().getData().getTeams().size()).isEqualTo(1);
		// assertThat(response.getBody().getData().getTotalPages()).isEqualTo(1);
		// assertThat(response.getBody().getData().getTotalElements()).isEqualTo(10);

		verify(teamService, times(1)).getAllTeams(0, 10, null, null, null);
	}

	@Test
	void getOrderById() {
		Long teamId = 1L;
		when(teamService.getTeamById(teamId)).thenReturn(teamResponseDto);

		ResponseEntity<ApiResult<TeamResponseDto>> response =
			teamController.getOrderById(teamId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMessage()).isEqualTo("팀 단건 조회 완료");
		assertEquals(teamResponseDto, response.getBody().getData());

		verify(teamService, times(1)).getTeamById(teamId);
	}

	@Test
	void updateTeam() {
		Long teamId = 1L;
		when(teamService.updateTeam(teamId, teamRequestDto, userDetails)).thenReturn(teamResponseDto);

		ResponseEntity<ApiResult<TeamResponseDto>> response =
			teamController.updateTeam(teamId, teamRequestDto, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMessage()).isEqualTo("팀 수정 완료");
		assertEquals(teamResponseDto, response.getBody().getData());

		verify(teamService, times(1)).updateTeam(teamId, teamRequestDto, userDetails);
	}

	@Test
	void deleteTeam() {
		Long teamId = 1L;
		when(teamService.deleteTeam(teamId, userDetails)).thenReturn(deleteResponseDto);

		ResponseEntity<ApiResult<DeleteResponseDto>> response =
			teamController.deleteTeam(teamId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getMessage()).isEqualTo("팀 삭제 완료");
		assertEquals(deleteResponseDto, response.getBody().getData());

		verify(teamService, times(1)).deleteTeam(teamId, userDetails);
	}
}