package com.sparta.sportify.service.teamChat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RedissonClient;

import com.sparta.sportify.entity.team.Team;
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

	}
}