package com.sparta.sportify.service.teamArticle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamArticle.TeamArticle;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.TeamArticleRepository;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamArticleService;

class TeamArticleServiceTest {

	@InjectMocks
	private TeamArticleService teamArticleService;

	@Mock
	private TeamRepository teamRepository;
	@Mock
	private TeamMemberRepository teamMemberRepository;
	@Mock
	private TeamArticleRepository teamArticleRepository;

	private TeamArticle teamArticle;
	private TeamMember teamMember;
	private TeamArticleResponseDto teamArticleResponseDto;
	private Team team;
	private User user;
	private User user2;
	private UserDetailsImpl userDetails;
	private UserDetailsImpl notOwnerUserDetails;

	private TeamArticleRequestDto teamArticleRequestDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		teamArticleRequestDto = new TeamArticleRequestDto("제목", "내용");

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
		when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

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

		user2 = User.builder()
			.id(100L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L)
			.build();
		notOwnerUserDetails = new UserDetailsImpl(user2.getName(), user2.getRole(), user2);

		teamMember = TeamMember.builder()
			.teamMemberId(1L)
			.teamMemberRole(TeamMemberRole.USER)
			.deletedAt(null)
			.status(TeamMember.Status.APPROVED)
			.user(userDetails.getUser())
			.team(team)
			.build();
		when(teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), team.getId())).thenReturn(
			Optional.of(teamMember));

		teamArticle = TeamArticle.builder()
			.id(1L)
			.title("제목")
			.content("내용")
			.createAt(LocalDateTime.now())
			.deletedAt(null)
			.user(userDetails.getUser())
			.team(team)
			.build();
		when(teamArticleRepository.save(any(TeamArticle.class))).thenReturn(teamArticle);
	}

	@Test
	@DisplayName("팀 게시글 작성 성공")
	void createPost() {
		TeamArticleResponseDto responseDto = teamArticleService.createPost(team.getId(), userDetails,
			teamArticleRequestDto);

		assertNotNull(responseDto);
		assertEquals(teamArticleRequestDto.getTitle(), responseDto.getTitle());
		assertEquals(teamArticleRequestDto.getContent(), responseDto.getContent());
		assertEquals(userDetails.getUser().getId(), responseDto.getUserId());
	}

	@Test
	@DisplayName("팀 게시글 모두 조회")
	void getPostAll() {
		int page = 1;
		int size = 10;

		Page<TeamArticle> mockPage = new PageImpl<>(List.of(teamArticle));
		when(teamArticleRepository.findAllByTeamId(team.getId(),
			PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createAt"))))
			.thenReturn(mockPage);

		Page<TeamArticleResponseDto> responsePage = teamArticleService.getPostAll(team.getId(), userDetails, page,
			size);

		assertNotNull(responsePage);
		assertEquals(1, responsePage.getTotalElements());
		TeamArticleResponseDto responseDto = responsePage.getContent().get(0);
		assertEquals(teamArticle.getTitle(), responseDto.getTitle());
		assertEquals(teamArticle.getContent(), responseDto.getContent());
		assertEquals(teamArticle.getUser().getId(), responseDto.getUserId());
	}

	@Test
	@DisplayName("게시글이 존재하지 않을 경우 예외 발생")
	void getPost_NotFound() {
		Long teamId = team.getId();
		int page = 1;
		int size = 10;

		Page<TeamArticle> emptyPage = new PageImpl<>(new ArrayList<>());
		when(teamArticleRepository.findAllByTeamId(teamId,
			PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createAt"))))
			.thenReturn(emptyPage);

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			teamArticleService.getPostAll(teamId, userDetails, page, size);
		});
		assertEquals(ErrorCode.POST_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("팀 게시글 업데이트")
	void updatePost() {
		TeamArticleRequestDto updateRequest = new TeamArticleRequestDto("수정된 제목", "수정된 내용");

		when(teamArticleRepository.findById(teamArticle.getId())).thenReturn(Optional.of(teamArticle));
		when(teamArticleRepository.save(any(TeamArticle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TeamArticleResponseDto responseDto = teamArticleService.updatePost(teamArticle.getId(), updateRequest,
			userDetails);

		assertNotNull(responseDto);
		assertEquals(updateRequest.getTitle(), responseDto.getTitle());
		assertEquals(updateRequest.getContent(), responseDto.getContent());
		assertEquals(userDetails.getUser().getId(), responseDto.getUserId());
	}

	@Test
	@DisplayName("다른 사용자의 게시글 수정 시 예외 발생")
	void updatePost_NotOwner() {
		TeamArticleRequestDto updateRequest = new TeamArticleRequestDto("수정된 제목", "수정된 내용");

		when(teamArticleRepository.findById(teamArticle.getId())).thenReturn(Optional.of(teamArticle));

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			teamArticleService.updatePost(teamArticle.getId(), updateRequest, notOwnerUserDetails);
		});

		assertEquals(ErrorCode.ONLY_OWN_POST_CAN_BE_MODIFIED, thrown.getErrorCode());
	}

	@Test
	@DisplayName("팀 게시글 삭제")
	void deletePost() {
		when(teamArticleRepository.findById(teamArticle.getId())).thenReturn(Optional.of(teamArticle));
		when(teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), team.getId()))
			.thenReturn(Optional.of(teamMember));
		when(teamArticleRepository.save(any(TeamArticle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TeamArticleResponseDto responseDto = teamArticleService.deletePost(teamArticle.getId(), userDetails);

		assertNotNull(responseDto);
		assertNotNull(teamArticle.getDeletedAt());
	}

	@Test
	@DisplayName("다른 사용자의 게시글 삭제 시 예외 발생")
	void deletePost_NotOwner() {
		when(teamArticleRepository.findById(teamArticle.getId())).thenReturn(Optional.of(teamArticle));

		CustomApiException exception = assertThrows(CustomApiException.class,
			() -> teamArticleService.deletePost(teamArticle.getId(), notOwnerUserDetails));
		assertEquals(ErrorCode.ONLY_OWN_POST_CAN_BE_DELETED, exception.getErrorCode());
	}

	@Test
	@DisplayName("팀 게시글 단건 조회")
	void getPost() {
		when(teamArticleRepository.findById(teamArticle.getId())).thenReturn(Optional.of(teamArticle));
		when(teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), team.getId()))
			.thenReturn(Optional.of(teamMember));
		when(teamArticleRepository.save(any(TeamArticle.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TeamArticleResponseDto responseDto = teamArticleService.getPost(team.getId(), teamArticle.getId(), userDetails);

		assertNotNull(responseDto);
		assertEquals(teamArticle.getTitle(), responseDto.getTitle());
		assertEquals(teamArticle.getContent(), responseDto.getContent());
	}
}