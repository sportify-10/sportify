package com.sparta.sportify.controller.teamArticle;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.teamArticle.TeamArticleService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/team/article")
@RequiredArgsConstructor
public class TeamArticleController {
	private final TeamArticleService teamArticleService;

	@PostMapping("{teamId}")
	public ResponseEntity<ApiResult<TeamArticleResponseDto>> createPost(
		@PathVariable Long teamId,
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody TeamArticleRequestDto teamArticleRequestDto
	){
		return ResponseEntity.ok(ApiResult.success("게시글 등록 성공", teamArticleService.createPost(teamId,userDetails, teamArticleRequestDto)));
	}
}
