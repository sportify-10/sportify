package com.sparta.sportify.controller.teamArticle;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamArticleService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team/article")
@RequiredArgsConstructor
public class TeamArticleController {
    private final TeamArticleService teamArticleService;

    @PostMapping("/{teamId}")
    public ResponseEntity<ApiResult<TeamArticleResponseDto>> createPost(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody TeamArticleRequestDto teamArticleRequestDto
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "게시글 등록 성공",
                        teamArticleService.createPost(teamId, userDetails, teamArticleRequestDto)
                )
        );
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResult<Page<TeamArticleResponseDto>>> getAllPosts(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                ApiResult.success("게시글 조회 성공",
                        teamArticleService.getPostAll(teamId, userDetails, page, size)
                )
        );
    }

    @GetMapping("/{teamId}/{articleId}")
    public ResponseEntity<ApiResult<TeamArticleResponseDto>> getPost(
            @PathVariable Long teamId,
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "게시글 단건 조회 성공",
                        teamArticleService.getPost(teamId, articleId, userDetails)
                )
        );
    }

    @PatchMapping("/{articleId}")
    public ResponseEntity<ApiResult<TeamArticleResponseDto>> updatePost(
            @PathVariable Long articleId,
            @RequestBody TeamArticleRequestDto teamArticleRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "게시글 수정 성공",
                        teamArticleService.updatePost(articleId, teamArticleRequestDto, userDetails)
                )
        );
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResult<TeamArticleResponseDto>> deletePost(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "게시글 삭제 성공",
                        teamArticleService.deletePost(articleId, userDetails)
                )
        );
    }
	@PostMapping("{teamId}")
	public ResponseEntity<ApiResult<TeamArticleResponseDto>> createPost(
		@PathVariable Long teamId,
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody TeamArticleRequestDto teamArticleRequestDto
	){
		return ResponseEntity.ok(ApiResult.success("게시글 등록 성공", teamArticleService.createPost(teamId,userDetails, teamArticleRequestDto)));
	}
}
