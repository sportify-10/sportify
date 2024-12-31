package com.sparta.sportify.service.teamChat;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.TeamChatRepository;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamChatService;

class TeamChatServiceTest {

	@InjectMocks
	private TeamChatService teamChatService;

	@Mock
	private TeamRepository teamRepository;
	@Mock
	private TeamMemberRepository teamMemberRepository;
	@Mock
	private TeamChatRepository teamChatRepository;
	@Mock
	private RedissonClient redissonClient;

	private TeamMember teamMember;
	private Team team;
	private User user;
	private User user2;
	private UserDetailsImpl userDetails;
	private UserDetailsImpl notOwnerUserDetails;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

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
	}

	@Test
	void joinTeamChatting() {
		assertDoesNotThrow(() -> {
			teamChatService.joinTeamChatting(team.getId(), userDetails);
		});
	}

	@Test
	void getChatData() {
		@SuppressWarnings("unchecked")//RList<Object>로 Mock 객체를 생성하여 타입 충돌 문제를 우회
		RList<Object> mockMessageList = Mockito.mock(RList.class);
		when(redissonClient.getList("team:" + team.getId() + ":messages")).thenReturn(mockMessageList);

		TeamChat teamChat = TeamChat.builder()
			.id(1L)
			.user(user)
			.team(team)
			.content("Test message")
			.createAt(LocalDateTime.of(2024, 12, 30, 16, 44, 0))
			.build();

		when(mockMessageList.isEmpty()).thenReturn(true);
		when(teamChatRepository.findByTeamId(team.getId())).thenReturn(Collections.singletonList(teamChat));

		List<TeamChatResponseDto> result = teamChatService.getChatData(team.getId(), userDetails);

		assertNotNull(result);
		assertThat(result.size()).isEqualTo(1); // DB에서 가져온 데이터가 추가됨
		assertThat(result.get(0).getContent()).isEqualTo("Test message");

		// Mock 레디스 리스트가 비어있지 않을 경우
		when(mockMessageList.isEmpty()).thenReturn(false);
		when(mockMessageList.stream()).thenReturn(Stream.of(
			new TeamChatResponseDto(user.getId(), team.getId(), "Cached message",
				LocalDateTime.of(2024, 12, 30, 16, 44, 0, 22000000))
		));

		// Act
		result = teamChatService.getChatData(team.getId(), userDetails);

		// Assert
		assertNotNull(result);
		assertThat(result.size()).isEqualTo(1); // Redis에서 가져온 데이터
		assertThat(result.get(0).getContent()).isEqualTo("Cached message");

		// Verify
		verify(teamMemberRepository, times(2)).findByUserIdAndTeamId(userDetails.getUser().getId(), team.getId());
		verify(mockMessageList, times(2)).isEmpty();
		verify(teamChatRepository, times(1)).findByTeamId(team.getId());
	}
}