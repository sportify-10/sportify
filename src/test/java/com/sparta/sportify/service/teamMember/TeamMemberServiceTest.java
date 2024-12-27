package com.sparta.sportify.service.teamMember;

import com.sparta.sportify.dto.teamDto.req.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.req.RoleRequestDto;
import com.sparta.sportify.dto.teamDto.res.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.res.RoleResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponsePage;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamMemberService;
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

class TeamMemberServiceTest {

    @InjectMocks
    private TeamMemberService teamMemberService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private User user;
    private User user1;
    private Team team;
    private UserDetailsImpl userDetails;
    private Long userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;

        user = new User();
        user.setId(1L);
        user.setCash(200000L);

        user1 = new User();
        user1.setId(2L);
        user1.setRole(UserRole.USER);
        user1.setCash(200000L);

        team = Team.builder()
                .id(1L)
                .teamName("Old Team")
                .region("Seoul")
                .activityTime("Weekends")
                .skillLevel("Intermediate")
                .sportType("Soccer")
                .description("Old description")
                .build();

        userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
    }

    @Test
    @DisplayName("팀 멤버 신청 - 성공")
    void applyToTeam_ShouldSucceed() {
        // Given
        TeamMember teamMember = TeamMember.builder()
                .user(user)
                .team(team)
                .build();

        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        // When
        teamMemberService.applyToTeam(user.getId(), userDetails);

        // Then
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("팀 멤버 신청 - 팀 없음 예외")
    void applyToTeam_TeamNotFound_ShouldThrowException() {
        // Given
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.applyToTeam(user.getId(), userDetails);
        });

        assertEquals("팀을 찾을 수 없습니다", thrown.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 조회 - 페이지네이션 성공")
    void getAllTeamMembers_ShouldReturnPagedTeamMembers() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        TeamMember teamMember = TeamMember.builder()
                .user(user)
                .team(team)
                .build();

        Page<TeamMember> teamMemberPage = new PageImpl<>(List.of(teamMember), pageable, 1);
        when(teamMemberRepository.findByTeamIdAndDeletedAtIsNull(team.getId(), pageable)).thenReturn(teamMemberPage);

        // When
        TeamMemberResponsePage responsePage = teamMemberService.getAllTeamMembers(page, size, team.getId());

        // Then
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(teamMember.getTeamMemberId(), responsePage.getTeamMembers().get(0).getTeamMemberId());
        assertEquals(team.getId(), responsePage.getTeamMembers().get(0).getTeamId());

        verify(teamMemberRepository, times(1)).findByTeamIdAndDeletedAtIsNull(team.getId(), pageable);
    }

    @Test
    @DisplayName("팀 멤버 조회 - 팀 없음 예외")
    void getAllTeamMembers_TeamNotFound_ShouldThrowException() {
        // Given
        int page = 0;
        int size = 10;
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.getAllTeamMembers(page, size, team.getId());
        });

        assertEquals("팀을 찾을 수 없습니다", thrown.getMessage());
    }

    @Test
    @DisplayName("팀 멤버 승인 - 성공")
    void approveOrRejectApplication_ShouldApproveSuccessfully() {
        // Given
        ApproveRequestDto requestDto = new ApproveRequestDto(user.getId());
        requestDto.setApprove(true);
        user1.setId(2L);
        user1.setRole(UserRole.USER);
        user1.setCash(200000L);
        userDetails = new UserDetailsImpl("user", user1.getRole(), user1);
        TeamMember approveMember = TeamMember.builder()
                .user(userDetails.getUser())
                .team(team)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .build();
        TeamMember applyMember = TeamMember.builder()
                .user(user)
                .team(team)
                .build();

        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserAndTeam(any(), any()))
                .thenReturn(Optional.of(approveMember))
                .thenReturn(Optional.of(applyMember));


        // When
        ApproveResponseDto responseDto = teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto);

        // Then
        assertNotNull(responseDto);
        verify(teamRepository, times(1)).findById(team.getId());
        verify(userRepository, times(1)).findById(user.getId());
        verify(teamMemberRepository, times(1)).findByUserAndTeam(user, team);

        assertEquals(user.getId(), responseDto.getUserId());
        assertTrue(responseDto.isApprove());
    }

    @Test
    @DisplayName("팀 멤버 승인 - 멤버 없음 예외")
    void approveTeamMember_TeamMemberNotFound_ShouldThrowException() {
        // Given
        ApproveRequestDto approveRequestDto = new ApproveRequestDto(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            teamMemberService.approveOrRejectApplication(team.getId(), userDetails, approveRequestDto);
        });

        assertEquals("팀원이 아닙니다", thrown.getMessage());
        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(user.getId(), team.getId());
    }


    @Test
    @DisplayName("권한 부여 성공 테스트")
    void grantRole_Success() {
        // given
        Long teamId = 1L;
        Long userId = 2L;
        RoleRequestDto requestDto = new RoleRequestDto(userId, "TEAM_MANAGER");

        User authUserEntity = new User();
        authUserEntity.setId(3L);
        when(userDetails.getUser()).thenReturn(authUserEntity);

        TeamMember requester = new TeamMember();
        requester.grantRole(TeamMemberRole.TEAM_OWNER);
        when(teamMemberRepository.findByUserIdAndTeamId(authUserEntity.getId(), teamId))
                .thenReturn(Optional.of(requester));

        TeamMember targetMember = new TeamMember();
        targetMember.grantRole(TeamMemberRole.USER);
        when(teamMemberRepository.findByUserIdAndTeamId(userId, teamId))
                .thenReturn(Optional.of(targetMember));

        // when
        RoleResponseDto responseDto = teamMemberService.grantRole(teamId, requestDto, userDetails);

        // then
        assertEquals(userId, responseDto.getUserId());
        assertEquals("TEAM_MANAGER", responseDto.getRole());
        verify(teamMemberRepository).save(targetMember);
    }
//    @Test
//    @DisplayName("팀 멤버 권한 부여 - 성공")
//    void assignRoleToTeamMember_ShouldSucceed() {
//        // Given
//        RoleRequestDto roleRequestDto = new RoleRequestDto(user.getId(), "ADMIN");
//        TeamMember teamMember = TeamMember.builder()
//                .user(user)
//                .team(team)
//                .build();
//
//
//        when(teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(Optional.of(teamMember));
//        doNothing().when(teamMemberRepository).grantRole(teamMember, "ADMIN");
//
//        // When
//        teamMemberService.grantRole(team.getId(), roleRequestDto, userDetails);
//
//        // Then
//        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(user.getId(), team.getId());
//        verify(teamMemberRepository, times(1)).grantRole(teamMember, "ADMIN");
//    }

//    @Test
//    @DisplayName("팀 멤버 권한 부여 - 멤버 없음 예외")
//    void assignRoleToTeamMember_TeamMemberNotFound_ShouldThrowException() {
//        // Given
//        RoleRequestDto roleRequestDto = new RoleRequestDto(user.getId(), "ADMIN");
//        when(teamMemberRepository.findByUserIdAndTeamId(user.getId(), team.getId())).thenReturn(Optional.empty());
//
//        // When & Then
//        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
//            teamMemberService.grantRole(team.getId(), roleRequestDto, userDetails);
//        });
//
//        assertEquals("팀 멤버를 찾을 수 없습니다", thrown.getMessage());
//        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(user.getId(), team.getId());
//    }
//
//    public void setUser1(User user1) {
//        this.user1 = user1;
//    }
}
