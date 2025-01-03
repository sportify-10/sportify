package com.sparta.sportify.controller.teamMember;

import com.sparta.sportify.dto.teamDto.req.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.req.RoleRequestDto;
import com.sparta.sportify.dto.teamDto.res.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.res.RoleResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponsePage;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamMemberService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @PostMapping("/grant/{teamId}")
    public ResponseEntity<ApiResult<TeamMemberResponseDto>> applyToTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl authUser) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀 신청 완료",
                        teamMemberService.applyToTeam(teamId, authUser)
                )
        );
    }

    @PostMapping("/approve/{teamId}")
    public ResponseEntity<ApiResult<ApproveResponseDto>> approveApplication(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @RequestBody ApproveRequestDto requestDto) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "처리가 완료되었습니다.",
                        teamMemberService.approveOrRejectApplication(teamId, authUser, requestDto)
                )
        );
    }

    @PatchMapping("/grant/{teamId}")
    public ResponseEntity<ApiResult<RoleResponseDto>> grantRole(
            @PathVariable Long teamId, // 팀 ID
            @RequestBody RoleRequestDto requestDto, // 사용자 ID와 역할 정보
            @AuthenticationPrincipal UserDetailsImpl authUser) { // 인증된 사용자// 요청자의 ID
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀 멤버 역할이 성공적으로 부여되었습니다.",
                        teamMemberService.grantRole(teamId, requestDto, authUser)
                )
        );
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResult<TeamMemberResponsePage>> getAllTeamMembers(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀 전체 조회 완료",
                        teamMemberService.getAllTeamMembers(page, size, teamId)
                )
        );
    }

    @DeleteMapping("/{teamId}/reject/{userId}")
    public ResponseEntity<ApiResult<TeamMemberResponseDto>> rejectTeamMember(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl authUser) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀원 퇴출 완료",
                        teamMemberService.rejectTeamMember(teamId, userId, authUser)
                )
        );
    }
}
