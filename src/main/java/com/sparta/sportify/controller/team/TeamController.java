package com.sparta.sportify.controller.team;

import com.sparta.sportify.dto.teamDto.req.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.res.DeleteResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponsePage;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {
	private final TeamService teamService;

	@PostMapping("/register")
	public ResponseEntity<ApiResult<TeamResponseDto>> createTeam(
		@RequestBody TeamRequestDto requestDto,
		@AuthenticationPrincipal UserDetailsImpl authUser) {

		Long creatorId = authUser.getUser().getId();
		return new ResponseEntity<>(
			ApiResult.success("팀 생성 완료",
				teamService.createTeam(requestDto, creatorId)),
			HttpStatus.OK);
	}

	@GetMapping()
	public ResponseEntity<ApiResult<TeamResponsePage>> getAllTeams(
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = "10") int size,
		@RequestParam(required = false) String sportType,
		@RequestParam(required = false) String skillLevel,
		@RequestParam(required = false) String region) {
		return new ResponseEntity<>(
			ApiResult.success("팀 전체 조회 완료",
				teamService.getAllTeams(page, size, sportType, skillLevel, region)),
			HttpStatus.OK);
	}

	@GetMapping("/{teamId}")
	public ResponseEntity<ApiResult<TeamResponseDto>> getOrderById(
		@PathVariable Long teamId
	) {
		return new ResponseEntity<>(
			ApiResult.success("팀 단건 조회 완료",
				teamService.getTeamById(teamId)),
			HttpStatus.OK);
	}

	@PatchMapping("/{teamId}")
	public ResponseEntity<ApiResult<TeamResponseDto>> updateTeam(
		@PathVariable Long teamid,
		@RequestBody TeamRequestDto requestDto,
		@AuthenticationPrincipal UserDetailsImpl authUser
	) {
		return new ResponseEntity<>(
			ApiResult.success("팀 수정 완료",
				teamService.updateTeam(teamid, requestDto, authUser)),
			HttpStatus.OK);

	}

	@DeleteMapping("/{teamId}")
	public ResponseEntity<ApiResult<DeleteResponseDto>> deleteTeam(
		@PathVariable Long teamId,
		@AuthenticationPrincipal UserDetailsImpl authUser
	) {
		return new ResponseEntity<>(
			ApiResult.success("팀 삭제 완료",
				teamService.deleteTeam(teamId, authUser)),
			HttpStatus.OK);

	}
}
