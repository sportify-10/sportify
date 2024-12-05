package com.sparta.sportify.controller;

import com.sparta.sportify.dto.teamDto.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.TeamResponsePage;
import com.sparta.sportify.service.TeamService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/register")
    public ResponseEntity<ApiResult<TeamResponseDto>> createMenu(@RequestBody TeamRequestDto requestDto) {
        return new ResponseEntity<>(
                ApiResult.success(
                        teamService.createTeam(requestDto)),
                HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<ApiResult<TeamResponsePage>> getAllMenus(@RequestParam(required = false, defaultValue = "0") int page,
                                                                   @RequestParam(required = false, defaultValue = "10") int size,
                                                                   @RequestParam(required = false, defaultValue = "modifiedAt") String criteria) {
        return new ResponseEntity<>(
                ApiResult.success(
                        teamService.getAllTeams(page, size, criteria)),
                HttpStatus.OK);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResult<TeamResponseDto>> getOrderById(@PathVariable Long teamId) {
        return new ResponseEntity<>(
                ApiResult.success(
                        teamService.getTeamById(teamId)),
                HttpStatus.OK);
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<ApiResult<TeamResponseDto>> updateTeam(@PathVariable Long teamid, @RequestBody TeamRequestDto requestDto) {
        teamService.updateTeam(teamid, requestDto);

        return new ResponseEntity<>(
                ApiResult.success(
                        teamService.updateTeam(teamid, requestDto)),
                HttpStatus.OK);

    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<ApiResult<TeamResponseDto>> deleteMenu(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return new ResponseEntity<>(
                ApiResult.success(
                        teamService.deleteTeam(teamId)),
                HttpStatus.OK);

    }
}
