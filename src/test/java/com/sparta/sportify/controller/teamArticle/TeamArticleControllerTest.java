package com.sparta.sportify.controller.teamArticle;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.hibernate.validator.internal.util.Contracts.*;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamArticle.TeamArticle;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamArticleService;
import com.sparta.sportify.util.api.ApiResult;

class TeamArticleControllerTest {

	@InjectMocks
	private TeamArticleController teamArticleController;

	@Mock
	private TeamArticleService teamArticleService;

	private TeamArticle teamArticle;
	private User user;
	private Team team;
	private UserDetailsImpl userDetails;
	private TeamArticleRequestDto teamArticleRequestDto;
	private TeamArticleResponseDto teamArticleResponseDto;
	private TeamArticleResponseDto teamArticleResponseDto2;

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

		team = Team.builder()
			.id(1L)
			.teamName("팀 이름")
			.region("지역")
			.activityTime("활동 시간")
			.skillLevel("스킬 레벨")
			.sportType("선호 종목")
			.description("설명")
			.teamPoints(1000)
			.winRate(0.9F)
			.deletedAt(null)
			.build();

		teamArticle = TeamArticle.builder()
			.id(1L)
			.title("제목")
			.content("내용")
			.createAt(LocalDateTime.of(2024, 12, 30, 16, 44, 0, 22000000))
			.deletedAt(null)
			.user(user)
			.team(team)
			.build();

		teamArticleRequestDto = new TeamArticleRequestDto("제목", "내용");

		teamArticleResponseDto = new TeamArticleResponseDto(teamArticle);
		teamArticleResponseDto2 = new TeamArticleResponseDto(teamArticle);
	}

	@Test
	void createPost() {
		when(teamArticleService.createPost(anyLong(), any(), any())).thenReturn(teamArticleResponseDto);

		ResponseEntity<ApiResult<TeamArticleResponseDto>> response = teamArticleController.createPost(1L, userDetails,
			teamArticleRequestDto);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("게시글 등록 성공");
		assertThat(response.getBody().getData()).isEqualTo(teamArticleResponseDto);
	}

	@Test
	void getAllPosts() {
		Page<TeamArticleResponseDto> page = new PageImpl<>(
			Arrays.asList(teamArticleResponseDto, teamArticleResponseDto2),
			PageRequest.of(0, 5), 2);

		when(teamArticleService.getPostAll(anyLong(), any(), anyInt(), anyInt())).thenReturn(page);

		ResponseEntity<ApiResult<Page<TeamArticleResponseDto>>> response = teamArticleController.getAllPosts(1L,
			userDetails, 1, 5);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("게시글 조회 성공");
		assertThat(response.getBody().getData().getContent().size()).isEqualTo(2);
	}

	@Test
	void getPost() {
		when(teamArticleService.getPost(anyLong(), anyLong(), any())).thenReturn(teamArticleResponseDto);

		ResponseEntity<ApiResult<TeamArticleResponseDto>> response = teamArticleController.getPost(1L, 1L, userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("게시글 단건 조회 성공");
		assertThat(response.getBody().getData()).isEqualTo(teamArticleResponseDto);
	}

	@Test
	void updatePost() {
		when(teamArticleService.updatePost(anyLong(), any(), any())).thenReturn(teamArticleResponseDto);

		ResponseEntity<ApiResult<TeamArticleResponseDto>> response = teamArticleController.updatePost(1L,
			teamArticleRequestDto, userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("게시글 수정 성공");
		assertThat(response.getBody().getData()).isEqualTo(teamArticleResponseDto);
	}

	@Test
	void deletePost() {
		when(teamArticleService.deletePost(anyLong(), any())).thenReturn(teamArticleResponseDto);

		ResponseEntity<ApiResult<TeamArticleResponseDto>> response = teamArticleController.deletePost(1L, userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertNotNull(response.getBody());
		assertThat(response.getBody().getMessage()).isEqualTo("게시글 삭제 성공");
		assertThat(response.getBody().getData()).isEqualTo(teamArticleResponseDto);
	}
}