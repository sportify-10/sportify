package com.sparta.sportify.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sparta.sportify.dto.user.res.UserTeamResponseDto;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.TeamMemberRole;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.UserService;

class UserServiceTest {

	@Mock
	TeamMemberRepository teamMemberRepository;

	@InjectMocks
	private UserService userService;

	private User user;
	private TeamMember teamMember1;
	private TeamMember teamMember2;
	private Team team1;
	private Team team2;
	private UserDetailsImpl userDetails;
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

		team1 = Team.builder()
			.id(1L)
			.teamName("A team")
			.region("Seoul")
			.skillLevel("Advanced")
			.sportType("Soccer")
			.description("허접 축구팀")
			.teamPoints(1000)
			.winRate(0.75F)
			.deletedAt(null)
			.teamMemberRole(TeamMemberRole.USER)
			.build();

		team2 = Team.builder()
			.id(2L)
			.teamName("B team")
			.region("Seoul")
			.skillLevel("Advanced")
			.sportType("Soccer")
			.description("허접 축구팀")
			.teamPoints(1000)
			.winRate(0.75F)
			.deletedAt(null)
			.teamMemberRole(TeamMemberRole.USER)
			.build();

		teamMember1 = TeamMember.builder()
			.teamMemberId(1L)
			.deletedAt(null)
			.status(TeamMember.Status.APPROVED)
			.teamMemberRole(TeamMemberRole.USER)
			.team(team1)
			.user(user)
			.build();

		teamMember2 = TeamMember.builder()
			.teamMemberId(2L)
			.deletedAt(null)
			.status(TeamMember.Status.APPROVED)
			.teamMemberRole(TeamMemberRole.USER)
			.team(team2)
			.user(user)
			.build();
	}

	@Test
	@DisplayName("자신이 속한 팀 조회 성공")
	void getUserTeams() {
		Pageable pageable = PageRequest.of(0, 10);

		List<TeamMember> teamMembers = List.of(teamMember1, teamMember2);

		when(teamMemberRepository.findTeams(userDetails.getUser().getId(), pageable))
			.thenReturn(new PageImpl<>(teamMembers));

		Page<UserTeamResponseDto> result = userService.getUserTeams(userDetails, 1, 10);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
	}
}