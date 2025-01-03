package com.sparta.sportify.service.teamMember;

import com.sparta.sportify.dto.teamDto.req.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.req.RoleRequestDto;
import com.sparta.sportify.dto.teamDto.res.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.res.RoleResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponsePage;
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
import com.sparta.sportify.service.TeamMemberService;
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

class TeamMemberServiceTest {

    @InjectMocks
    private TeamMemberService teamMemberService;

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private User approveUser;
    private User applyUser;
    private Team team;
    private TeamMember teamMember;
    private TeamMember teamMember2;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        approveUser = new User();
        approveUser.setId(1L);
        approveUser.setRole(UserRole.USER);
        approveUser.setCash(200000L);

        applyUser = new User();
        applyUser.setId(2L);
        applyUser.setRole(UserRole.USER);
        applyUser.setCash(200000L);

        team = Team.builder()
                .id(1L)
                .teamName("Old Team")
                .region("Seoul")
                .activityTime("Weekends")
                .skillLevel("Intermediate")
                .sportType("Soccer")
                .description("Old description")
                .build();
        teamMember = TeamMember.builder()
                .user(approveUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .build();
        teamMember2 = TeamMember.builder()
                .user(approveUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.USER)
                .build();

        userDetails = new UserDetailsImpl(approveUser.getName(), approveUser.getRole(), approveUser);
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.of(applyUser));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.findById(approveUser.getId())).thenReturn(Optional.of(teamMember));
        when(teamMemberRepository.findById(applyUser.getId())).thenReturn(Optional.of(teamMember2));
    }

    @Test
    @DisplayName("팀 멤버 신청 - 성공")
    void applyToTeam_ShouldSucceed() {
        // Given
        TeamMember teamMember = TeamMember.builder()
                .user(approveUser)
                .team(team)
                .build();

        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        // When
        teamMemberService.applyToTeam(approveUser.getId(), userDetails);

        // Then
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("isAlreadyPending이 true일 때 예외 발생")
    void addMember_AlreadyPending_ShouldThrowException() {
        // Given
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(teamMemberRepository.existsByUserAndTeamAndStatus(approveUser, team, TeamMember.Status.PENDING)).thenReturn(true);

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamMemberService.applyToTeam(team.getId(), userDetails));
        assertEquals(ErrorCode.ALREADY_PENDING, thrown.getErrorCode());
    }

    @Test
    @DisplayName("isAlreadyApproved가 true일 때 예외 발생")
    void addMember_AlreadyApproved_ShouldThrowException() {
        // Given
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(teamMemberRepository.existsByUserAndTeamAndStatus(approveUser, team, TeamMember.Status.APPROVED)).thenReturn(true);

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamMemberService.applyToTeam(team.getId(), userDetails));
        assertEquals(ErrorCode.ALREADY_MEMBER, thrown.getErrorCode());
    }


    @Test
    @DisplayName("팀 멤버 신청 - 팀 없음 예외")
    void applyToTeam_TeamNotFound_ShouldThrowException() {
        // Given
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
            teamMemberService.applyToTeam(approveUser.getId(), userDetails);
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
                .user(approveUser)
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
    @DisplayName("팀 멤버 승인 - 성공")
    void approveOrRejectApplication_ShouldApproveSuccessfully() {
        // Given
        ApproveRequestDto requestDto = new ApproveRequestDto(approveUser.getId(), true);
        applyUser.setId(2L);
        applyUser.setRole(UserRole.USER);
        applyUser.setCash(200000L);
        userDetails = new UserDetailsImpl("user", applyUser.getRole(), applyUser);
        TeamMember approveMember = TeamMember.builder()
                .user(userDetails.getUser())
                .team(team)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .build();
        TeamMember approveMember2 = TeamMember.builder()
                .user(userDetails.getUser())
                .team(team)
                .teamMemberRole(TeamMemberRole.MANAGER)
                .build();
        TeamMember applyMember = TeamMember.builder()
                .user(approveUser)
                .team(team)
                .build();
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(teamMemberRepository.findByUserAndTeam(any(), any()))
                .thenReturn(Optional.of(approveMember))
                .thenReturn(Optional.of(applyMember));
        when(teamMemberRepository.findByUserAndTeam(any(), any()))
                .thenReturn(Optional.of(approveMember2))
                .thenReturn(Optional.of(applyMember));
        // When
        ApproveResponseDto responseDto = teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto);
        // Then
        assertNotNull(responseDto);
        verify(teamRepository, times(1)).findById(team.getId());
        verify(userRepository, times(1)).findById(approveUser.getId());
        verify(teamMemberRepository, times(1)).findByUserAndTeam(approveUser, team);

        assertEquals(approveUser.getId(), responseDto.getUserId());
        assertTrue(responseDto.isApprove());

    }

    @Test
    @DisplayName("approveOrRejectApplication - TEAM_MEMBER 역할로 권한 부족 예외 발생")
    void approveOrRejectApplication_AsTeamMember_ShouldThrowInsufficientPermission() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.of(applyUser));
        when(teamMemberRepository.findByUserAndTeam(approveUser, team)).thenReturn(Optional.of(
                TeamMember.builder()
                        .user(approveUser)
                        .team(team)
                        .teamMemberRole(TeamMemberRole.USER)
                        .build()
        ));
        ApproveRequestDto requestDto = new ApproveRequestDto(approveUser.getId(), true);
        CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
            teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto);
        });
        assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, thrown.getErrorCode());
    }

    @Test
    @DisplayName("approveOrRejectApplication - USER_NOT_FOUND 예외 발생")
    void approveOrRejectApplication_UserNotFound_ShouldThrowException() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.empty());
        ApproveRequestDto requestDto = new ApproveRequestDto(applyUser.getId(), true);

        CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
            teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
    }

    @Test
    @DisplayName("isAlreadyApproved가 true일 때 예외 발생")
    void approve_AlreadyApproved_ShouldThrowException() {
        // Given
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(teamMemberRepository.existsByUserAndTeamAndStatus(any(), any(), any())).thenReturn(true);
        when(teamMemberRepository.findByUserAndTeam(approveUser, team)).thenReturn(Optional.of(
                TeamMember.builder()
                        .user(approveUser)
                        .team(team)
                        .status(TeamMember.Status.APPROVED)
                        .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                        .build()
        ));
        ApproveRequestDto requestDto = new ApproveRequestDto(approveUser.getId(), true);
        // When & Then
        CustomApiException thrown = assertThrows(CustomApiException.class,
                () -> teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto));
        assertEquals(ErrorCode.ALREADY_MEMBER, thrown.getErrorCode());
    }

    @Test
    @DisplayName("팀원 승인거부")
    void testRejectApplication() {
        ApproveRequestDto requestDto = new ApproveRequestDto(applyUser.getId(), false);

        TeamMember applicantTeamMember = TeamMember.builder()
                .user(applyUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.USER)
                .status(TeamMember.Status.PENDING)
                .build();

        // Mocking
        when(teamMemberRepository.findByUserAndTeam(approveUser, team)).thenReturn(Optional.of(teamMember));
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.of(applyUser));
        when(teamMemberRepository.findByUserAndTeam(applyUser, team)).thenReturn(Optional.of(applicantTeamMember));
        when(teamMemberRepository.existsByUserAndTeamAndStatus(any(), any(), any())).thenReturn(false);

        // Act
        ApproveResponseDto response = teamMemberService.approveOrRejectApplication(team.getId(), userDetails, requestDto);

        // Assert
        assertNotNull(response);
        assertEquals(applyUser.getId(), response.getUserId());
        assertEquals(false, response.isApprove());
        assertEquals(TeamMember.Status.REJECTED, applicantTeamMember.getStatus());

        // Verify interactions
        verify(teamMemberRepository, times(1)).save(applicantTeamMember);
    }

    @Test
    @DisplayName("grantRole - TEAM_OWNER가 역할 부여 성공")
    void grantRole_TeamOwnerGrantRole_ShouldSucceed() {
        // Given
        Long teamId = team.getId();
        Long targetUserId = applyUser.getId();

        when(teamMemberRepository.findByUserIdAndTeamId(approveUser.getId(), teamId)).thenReturn(Optional.of(
                TeamMember.builder()
                        .user(approveUser)
                        .team(team)
                        .teamMemberRole(TeamMemberRole.TEAM_OWNER) // 요청자가 TEAM_OWNER
                        .build()
        ));
        when(teamMemberRepository.findByUserIdAndTeamId(targetUserId, teamId)).thenReturn(Optional.of(
                TeamMember.builder()
                        .user(applyUser)
                        .team(team)
                        .teamMemberRole(TeamMemberRole.USER) // 대상자는 TEAM_MEMBER
                        .build()
        ));

        RoleRequestDto requestDto = new RoleRequestDto(targetUserId, teamMember2.getTeamMemberRole());

        // When
        RoleResponseDto response = teamMemberService.grantRole(teamId, requestDto, userDetails);

        // Then
        assertNotNull(response);
        assertEquals(targetUserId, response.getUserId());
        assertEquals(teamMember2.getTeamMemberRole(), response.getRole());
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("grantRole - TEAM_MEMBER 역할로 권한 부족 예외 발생")
    void grantRole_AsTeamMember_ShouldThrowInsufficientPermission() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.of(applyUser));
        when(teamMemberRepository.findByUserIdAndTeamId(any(), any())).thenReturn(Optional.of(teamMember2));
        when(teamMemberRepository.findByUserAndTeam(approveUser, team)).thenReturn(Optional.of(
                TeamMember.builder()
                        .user(approveUser)
                        .team(team)
                        .teamMemberRole(TeamMemberRole.USER)
                        .build()
        ));
        RoleRequestDto requestDto = new RoleRequestDto(applyUser.getId(), teamMember2.getTeamMemberRole());
        CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
            teamMemberService.grantRole(team.getId(), requestDto, userDetails);
        });
        assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, thrown.getErrorCode());
    }

    @Test
    @DisplayName("rejectTeamMember - 퇴출 성공")
    void testRejectTeamMemberSuccess() {

        TeamMember approveTeamMember = TeamMember.builder()
                .user(approveUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER) // 요청자는 팀장
                .build();

        TeamMember applyTeamMember = TeamMember.builder()
                .teamMemberId(teamMember2.getTeamMemberId())
                .user(applyUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.USER) // 대상자는 일반 팀원
                .build();

        // Mocking
        when(userRepository.findById(applyUser.getId())).thenReturn(Optional.of(applyUser));
        when(userRepository.findById(approveUser.getId())).thenReturn(Optional.of(approveUser));
        when(teamMemberRepository.findByUserIdAndTeamId(approveUser.getId(), team.getId())).thenReturn(Optional.of(approveTeamMember));
        when(teamMemberRepository.findByUserIdAndTeamId(applyUser.getId(), team.getId())).thenReturn(Optional.of(applyTeamMember));

        // Act
        TeamMemberResponseDto response = teamMemberService.rejectTeamMember(team.getId(), applyUser.getId(), userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(applyTeamMember.getTeamMemberId(), response.getTeamMemberId());
        assertEquals(team.getId(), response.getTeamId());
        assertNotNull(applyTeamMember.getDeletedAt());
        verify(teamMemberRepository, times(1)).save(applyTeamMember); // 소프트 삭제가 저장되었는지 확인
    }

    @Test
    @DisplayName("rejectTeamMember - Not a Team Member 예외처리")
    void testRejectTeamMemberNotTeamMember() {
        // Arrange
        Long teamId = 1L;
        Long targetUserId = 2L;

        when(teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class, () -> {
            teamMemberService.rejectTeamMember(teamId, targetUserId, userDetails);
        });

        // 예외 검증
        assertEquals(ErrorCode.NOT_TEAM_MEMBER, exception.getErrorCode());
        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(userDetails.getUser().getId(), teamId);
    }

    @Test
    @DisplayName("rejectTeamMember - TEAM_MEMBER 역할로 권한 부족 예외 발생")
    void testRejectTeamMemberInsufficientPermission() {
        // Arrange
        Long teamId = 1L;
        Long targetUserId = 2L;

        Team team = Team.builder()
                .id(teamId)
                .teamName("Test Team")
                .build();

        User requesterUser = new User();
        requesterUser.setId(1L);

        TeamMember requester = TeamMember.builder()
                .user(requesterUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.USER) // 요청자가 팀장이 아님
                .build();

        when(teamMemberRepository.findByUserIdAndTeamId(requesterUser.getId(), teamId))
                .thenReturn(Optional.of(requester));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class, () -> {
            teamMemberService.rejectTeamMember(teamId, targetUserId, userDetails);
        });

        // 예외 검증
        assertEquals(ErrorCode.INSUFFICIENT_PERMISSION, exception.getErrorCode());
        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(requesterUser.getId(), teamId);
    }

    @Test
    @DisplayName("rejectTeamMember - 퇴출 대상 팀원 조회 실패")
    void testRejectTeamMemberTargetNotFound() {
        // Arrange
        Long teamId = 1L;
        Long targetUserId = 2L;

        Team team = Team.builder()
                .id(teamId)
                .teamName("Test Team")
                .build();

        User requesterUser = new User();
        requesterUser.setId(1L);

        TeamMember requester = TeamMember.builder()
                .user(requesterUser)
                .team(team)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER) // 요청자는 팀장
                .build();

        when(teamMemberRepository.findByUserIdAndTeamId(requesterUser.getId(), teamId))
                .thenReturn(Optional.of(requester));
        when(teamMemberRepository.findByUserIdAndTeamId(targetUserId, teamId))
                .thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class, () -> {
            teamMemberService.rejectTeamMember(teamId, targetUserId, userDetails);
        });

        // 예외 검증
        assertEquals(ErrorCode.NOT_TEAM_MEMBER, exception.getErrorCode());
        verify(teamMemberRepository, times(1)).findByUserIdAndTeamId(targetUserId, teamId);
    }
}
