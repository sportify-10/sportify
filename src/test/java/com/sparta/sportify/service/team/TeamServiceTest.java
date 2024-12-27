package com.sparta.sportify.service.team;

import com.sparta.sportify.dto.teamDto.req.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
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

        // Mock User 객체 생성
        user = User.builder().id(2L).cash(20000L).build();

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
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> teamService.getTeamById(1L));
        assertEquals("팀을 찾을 수 없습니다.", thrown.getMessage());

        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateTeam 메소드의 정상 동작을 확인합니다.")
    void updateTeam_ShouldUpdateAndReturnTeamResponseDto() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));

        // When
        TeamResponseDto responseDto = teamService.updateTeam(1L, requestDto);

        // Then
        assertNotNull(responseDto);
        assertEquals("Test Team", responseDto.getTeamName());
        assertEquals("Seoul", responseDto.getRegion());

        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("deleteTeam 메소드의 정상 동작을 확인합니다.")
    void deleteTeam_ShouldSoftDeleteAndReturnTeamResponseDto() {
        // Given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(existingTeam));

        // When
        TeamResponseDto responseDto = teamService.deleteTeam(1L);

        // Then
        assertNotNull(responseDto);
        assertNotNull(existingTeam.getDeletedAt());

        verify(teamRepository, times(1)).findById(1L);
    }
}
