package com.sparta.sportify.controller.teamMember;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.teamDto.req.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.req.RoleRequestDto;
import com.sparta.sportify.dto.teamDto.res.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.res.RoleResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponsePage;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeamMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamMemberService teamMemberService;

    @Mock
    private UserDetailsImpl userDetails;
    private TeamMemberResponseDto teamMemberResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        teamMemberResponseDto = new TeamMemberResponseDto(1L, 1L, TeamMember.Status.APPROVED);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testReservationProcessPersonal() throws Exception {
        Long teamId = 1L;
        Long teamMemberId = 1L;
        TeamMember.Status status = TeamMember.Status.PENDING;
        TeamMemberResponseDto responseDto = new TeamMemberResponseDto(teamId, teamMemberId, status);

        when(teamMemberService.applyToTeam(any(), any())).thenReturn(responseDto);
        mockMvc.perform(post("/api/teams/grant/{teamId}", teamId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("팀 신청 완료"))
                .andExpect(jsonPath("$.data.teamId").value(teamId));

    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testApproveApplication() throws Exception {
        // Given
        Long teamId = 1L;
        ApproveRequestDto requestDto = new ApproveRequestDto(2L, true); // 예: teamMemberId=2, approve=true
        ApproveResponseDto responseDto = new ApproveResponseDto(2L, true);

        // Mocking service behavior
        when(teamMemberService.approveOrRejectApplication(any(), any(), any())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/teams/approve/{teamId}", teamId)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(requestDto))) // JSON body 설정
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("처리가 완료되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.approve").value(true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testGrantRole() throws Exception {
        // Given
        Long teamId = 1L;
        RoleRequestDto requestDto = new RoleRequestDto(2L, TeamMemberRole.TEAM_OWNER); // 요청 데이터: 사용자 ID와 역할
        RoleResponseDto responseDto = new RoleResponseDto(2L, TeamMemberRole.MANAGER); // 응답 데이터: 사용자 ID와 역할

        // Mocking service behavior
        when(teamMemberService.grantRole(any(), any(), any())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/teams/grant/{teamId}", teamId)
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(requestDto))) // JSON 요청 본문
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("팀 멤버 역할이 성공적으로 부여되었습니다.")) // 메시지 검증
                .andExpect(jsonPath("$.data.userId").value(2)) // userId 검증
                .andExpect(jsonPath("$.data.role").value(TeamMemberRole.MANAGER.toString())); // role 검증
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testGetAllTeamMembers() throws Exception {
        // Given
        Long teamId = 1L;
        int page = 0;
        int size = 10;

        List<TeamMemberResponseDto> teamMembers = List.of(
                new TeamMemberResponseDto(1L, 1L, TeamMember.Status.APPROVED),
                new TeamMemberResponseDto(2L, 2L, TeamMember.Status.APPROVED)
        );
        TeamMemberResponsePage responsePage = new TeamMemberResponsePage();
        ReflectionTestUtils.setField(responsePage, "teamMembers", teamMembers);
        ReflectionTestUtils.setField(responsePage, "totalPages", 1);
        ReflectionTestUtils.setField(responsePage, "totalElements", 2L);

        // Mocking service behavior
        when(teamMemberService.getAllTeamMembers(page, size, teamId)).thenReturn(responsePage);

        // When & Then
        mockMvc.perform(get("/api/teams/{teamId}/members", teamId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("팀 전체 조회 완료")) // 메시지 검증
                .andExpect(jsonPath("$.data.totalPages").value(1)) // 총 페이지 수 검증
                .andExpect(jsonPath("$.data.totalElements").value(2)) // 총 요소 수 검증
                .andExpect(jsonPath("$.data.teamMembers[0].teamMemberId").value(1L))
                .andExpect(jsonPath("$.data.teamMembers[0].teamId").value(teamId))// 첫 번째 멤버 ID 검증
                .andExpect(jsonPath("$.data.teamMembers[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.data.teamMembers[0].teamId").value(teamId))// 첫 번째 멤버 역할 검증
                .andExpect(jsonPath("$.data.teamMembers[1].teamMemberId").value(2L)) // 두 번째 멤버 ID 검증
                .andExpect(jsonPath("$.data.teamMembers[1].status").value("APPROVED")); // 두 번째 멤버 역할 검증
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testRejectTeamMember() throws Exception {
        // Given
        Long teamId = 1L;
        Long userId = 2L;

        TeamMemberResponseDto responseDto = new TeamMemberResponseDto(userId, teamId, TeamMember.Status.REJECTED);

        // Mocking service behavior
        when(teamMemberService.rejectTeamMember(any(), any(), any()))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(delete("/api/teams/{teamId}/reject/{userId}", teamId, userId)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("팀원 퇴출 완료")) // 성공 메시지 검증
                .andExpect(jsonPath("$.data.teamMemberId").value(userId)) // 퇴출된 사용자 ID 검증
                .andExpect(jsonPath("$.data.teamId").value(teamId)) // 팀 ID 검증
                .andExpect(jsonPath("$.data.status").value("REJECTED")); // 상태 검증
    }

}
