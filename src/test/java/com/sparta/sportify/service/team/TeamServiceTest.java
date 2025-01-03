package com.sparta.sportify.service.team;

import com.sparta.sportify.dto.teamDto.req.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.res.DeleteResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponsePage;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    private UserDetailsImpl userDetails;

    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;

    private TeamRequestDto requestDto;

    private TeamMember teamMember;
    private Team existingTeam;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = new TeamRequestDto("Test Team", "Seoul", "Weekends", "Intermediate", "Soccer", "Test description");

        existingTeam = Team.builder()
                .id(1L)
                .teamName("Old Team")
                .region("Seoul")
                .activityTime("Weekends")
                .skillLevel("Intermediate")
                .sportType("Soccer")
                .description("Old description")
                .build();
        teamMember = TeamMember.builder()
                .user(user)
                .team(existingTeam)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .build();
        user = User.builder().id(1L).role(UserRole.USER).cash(20000L).build();
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.of(teamMember));
        // Mock User 객체 생성
        userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
    }

    @Test
    @DisplayName("createTeam 메소드의 정상 동작을 확인합니다.")
    void createTeam_ShouldReturnTeamResponseDto() {
        // Given
        Team team = Team.builder()
                .teamName(requestDto.getTeamName())
                .region(requestDto.getRegion())
                .activityTime(requestDto.getActivityTime())
                .skillLevel(requestDto.getSkillLevel())
                .sportType(requestDto.getSportType())
                .description(requestDto.getDescription())
                .build();
        TeamMember teamMember = TeamMember.builder()
                .teamMemberId(1L)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .team(team)
                .build();
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        // When
        TeamResponseDto responseDto = teamService.createTeam(requestDto, 1L);

        // Then
        assertNotNull(responseDto);
        assertEquals("Test Team", responseDto.getTeamName());
        assertEquals("Seoul", responseDto.getRegion());

        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @DisplayName("getTeamById 메소드의 정상 동작을 확인합니다.")
    void getTeamById_ShouldReturnTeamResponseDto() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));

        // When
        TeamResponseDto responseDto = teamService.getTeamById(1L);

        // Then
        assertNotNull(responseDto);
        assertEquals("Old Team", responseDto.getTeamName());
        assertEquals("Seoul", responseDto.getRegion());

        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID가 존재하지 않을 때 예외를 발생시키는지 확인합니다.")
    void getTeamById_TeamNotFound_ShouldThrowException() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));


        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class, () -> teamService.getTeamById(1L));
        assertEquals("팀을 찾을 수 없습니다", thrown.getMessage());

        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getAllTeams 메소드의 정상 동작을 확인합니다.")
    void getAllTeams_ShouldReturnTeamResponsePage() {
        // Given
        int page = 0;
        int size = 10;
        String sportType = "Soccer";
        String skillLevel = "Intermediate";
        String region = "Seoul";

        Pageable pageable = PageRequest.of(page, size);
        List<Team> teamList = List.of(
                Team.builder().id(1L).teamName("Team A").region("Seoul").sportType("Soccer").skillLevel("Intermediate").build(),
                Team.builder().id(2L).teamName("Team B").region("Seoul").sportType("Soccer").skillLevel("Intermediate").build()
        );
        Page<Team> mockPage = new PageImpl<>(teamList, pageable, teamList.size());

        when(teamRepository.findAllWithFilters(sportType, skillLevel, region, pageable)).thenReturn(mockPage);

        // When
        TeamResponsePage responsePage = teamService.getAllTeams(page, size, sportType, skillLevel, region);

        // Then
        assertNotNull(responsePage, "ResponsePage는 null이 아니어야 합니다.");
        assertEquals(2, responsePage.getTeams().size(), "반환된 팀 개수가 일치하지 않습니다.");
        assertEquals("Team A", responsePage.getTeams().get(0).getTeamName(), "첫 번째 팀 이름이 일치하지 않습니다.");
        assertEquals("Seoul", responsePage.getTeams().get(0).getRegion(), "첫 번째 팀 지역이 일치하지 않습니다.");

        verify(teamRepository, times(1)).findAllWithFilters(sportType, skillLevel, region, pageable);
    }

    @Test
    @DisplayName("updateTeam 메소드의 정상 동작을 확인합니다.")
    void updateTeam_ShouldUpdateAndReturnTeamResponseDto() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.of(teamMember));

        // When
        TeamResponseDto responseDto = teamService.updateTeam(1L, requestDto, userDetails);

        // Then
        assertNotNull(responseDto);
        assertEquals("Test Team", responseDto.getTeamName());
        assertEquals("Seoul", responseDto.getRegion());

        verify(teamRepository, times(1)).findById(1L);
        verify(teamMemberRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("updateTeam - 존재하지 않는 유저로 예외 발생")
    void updateTeam_UserNotFound_ShouldThrowException() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.empty()); // 유저를 찾지 못함

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamService.updateTeam(1L, requestDto, userDetails));
        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode(), "유저를 찾을 수 없다는 예외가 발생해야 합니다.");

        verify(teamMemberRepository, times(1)).findById(user.getId());
    }


    @Test
    @DisplayName("updateTeam - 권한이 부족할 때 예외 발생")
    void updateTeam_InsufficientPermission_ShouldThrowException() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Mock TeamMember (권한 부족)
        TeamMember teamMember = TeamMember.builder()
                .teamMemberId(1L)
                .teamMemberRole(TeamMemberRole.USER) // 팀 소유자가 아님
                .team(existingTeam)
                .build();
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.of(teamMember));

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamService.updateTeam(1L, requestDto, userDetails));
        assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, thrown.getErrorCode(), "권한 부족 예외가 발생해야 합니다.");

        verify(teamMemberRepository, times(1)).findById(user.getId());
    }


    @Test
    @DisplayName("deleteTeam 메소드의 정상 동작을 확인합니다.")
    void deleteTeam_ShouldSoftDeleteAndReturnTeamResponseDto() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
        // When
        DeleteResponseDto responseDto = teamService.deleteTeam(1L, userDetails);

        // Then
        assertNotNull(responseDto);
        assertNotNull(existingTeam.getDeletedAt());

        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("deleteTeam - 존재하지 않는 유저로 예외 발생")
    void deleteTeam_UserNotFound_ShouldThrowException() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.empty()); // 유저를 찾지 못함

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamService.deleteTeam(1L, userDetails));
        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode(), "유저를 찾을 수 없다는 예외가 발생해야 합니다.");

        verify(teamMemberRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("deleteTeam - 권한이 부족할 때 예외 발생")
    void deleteTeam_InsufficientPermission_ShouldThrowException() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Mock TeamMember (권한 부족)
        TeamMember teamMember = TeamMember.builder()
                .teamMemberId(1L)
                .teamMemberRole(TeamMemberRole.USER) // 팀 소유자가 아님
                .team(existingTeam)
                .build();
        when(teamMemberRepository.findById(user.getId())).thenReturn(Optional.of(teamMember));

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamService.deleteTeam(1L, userDetails));
        assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, thrown.getErrorCode(), "권한 부족 예외가 발생해야 합니다.");

        verify(teamMemberRepository, times(1)).findById(user.getId());
    }
}
