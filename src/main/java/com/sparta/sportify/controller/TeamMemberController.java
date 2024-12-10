package com.sparta.sportify.controller;

import com.sparta.sportify.dto.teamDto.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.TeamMemberResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamMemberService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        Long userId = authUser.getUser().getId();
        return new ResponseEntity<>(
                ApiResult.success("팀 신청 완료",
                        teamMemberService.applyToTeam(teamId, userId)),
                HttpStatus.OK);
    }

    @PostMapping("/approve/{teamId}")
    public ResponseEntity<ApiResult<ApproveResponseDto>> approveApplication(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @RequestBody ApproveRequestDto requestDto) {
        Long approveId = authUser.getUser().getId();
        return new ResponseEntity<>(
                ApiResult.success("처리가 완료되었습니다.",
                        teamMemberService.approveOrRejectApplication(teamId, approveId, requestDto)),
                HttpStatus.OK
        );
    }
}
