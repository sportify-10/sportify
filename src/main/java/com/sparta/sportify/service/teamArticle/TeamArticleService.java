package com.sparta.sportify.service.teamArticle;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.entity.teamArticle.TeamArticle;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.teamArticle.TeamArticleRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamArticleService {

	private final TeamMemberRepository teamMemberRepository;
	private final TeamArticleRepository teamArticleRepository;

	public TeamArticleResponseDto createPost(Long teamId, UserDetailsImpl userDetails, TeamArticleRequestDto teamArticleRequestDto) {
		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(),teamId)
			.orElseThrow(()->new IllegalArgumentException("팀 멤버만 작성 가능합니다"));

		TeamArticle teamArticle = teamArticleRepository.save(
			TeamArticle.builder()
				.title(teamArticleRequestDto.getTitle())
				.content(teamArticleRequestDto.getContent())
				.userName(userDetails.getUser().getName())
				.createAt(LocalDateTime.now())
				.build());

		return new TeamArticleResponseDto(teamArticle);
	}
}
