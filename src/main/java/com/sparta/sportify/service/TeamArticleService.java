package com.sparta.sportify.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamArticle.TeamArticle;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.TeamArticleRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamArticleService {

	private final TeamMemberRepository teamMemberRepository;
	private final TeamArticleRepository teamArticleRepository;
	private final TeamRepository teamRepository;

	public TeamArticleResponseDto createPost(Long teamId, UserDetailsImpl userDetails,
		TeamArticleRequestDto teamArticleRequestDto) {
		Team team = teamRepository.findById(teamId).orElseThrow(() -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND));

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.ONLY_TEAM_MEMBER_CAN_WRITE));

		TeamArticle teamArticle = teamArticleRepository.save(
			TeamArticle.builder()
				.title(teamArticleRequestDto.getTitle())
				.content(teamArticleRequestDto.getContent())
				.user(userDetails.getUser())
				.team(team)
				.createAt(LocalDateTime.now())
				.build());

		return new TeamArticleResponseDto(teamArticle);
	}

	public Page<TeamArticleResponseDto> getPostAll(Long teamId, UserDetailsImpl userDetails, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createAt"));

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.ONLY_TEAM_MEMBER_CAN_VIEW));

		Page<TeamArticle> teamArticle = teamArticleRepository.findAllByTeamId(teamId, pageable);
		if (teamArticle.getContent().isEmpty()) {
			throw new CustomApiException(ErrorCode.POST_NOT_FOUND);
		}

		return teamArticle.map(TeamArticleResponseDto::new);
	}

	@Transactional
	public TeamArticleResponseDto updatePost(Long articleId, TeamArticleRequestDto teamArticleRequestDto,
		UserDetailsImpl userDetails) {
		TeamArticle teamArticle = teamArticleRepository.findById(articleId)
			.filter(article -> article.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

		if (teamArticle.getUser().getId() != userDetails.getUser().getId()) {
			throw new CustomApiException(ErrorCode.ONLY_OWN_POST_CAN_BE_MODIFIED);
		}

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamArticle.getTeam().getId())
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.ONLY_TEAM_MEMBER_CAN_MODIFY));

		teamArticle.updateOf(teamArticleRequestDto.getTitle(), teamArticleRequestDto.getContent(),
			userDetails.getUser(), teamArticle.getTeam());

		return new TeamArticleResponseDto(teamArticleRepository.save(teamArticle));
	}

	public TeamArticleResponseDto deletePost(Long articleId, UserDetailsImpl userDetails) {
		TeamArticle teamArticle = teamArticleRepository.findById(articleId)
			.filter(article -> article.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

		if (teamArticle.getUser().getId() != userDetails.getUser().getId()) {
			throw new CustomApiException(ErrorCode.ONLY_OWN_POST_CAN_BE_DELETED);
		}

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamArticle.getTeam().getId())
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.ONLY_TEAM_MEMBER_CAN_DELETE));

		teamArticle.deleteOf();

		return new TeamArticleResponseDto(teamArticleRepository.save(teamArticle));
	}

	public TeamArticleResponseDto getPost(Long teamId, Long articleId, UserDetailsImpl userDetails) {
		TeamArticle teamArticle = teamArticleRepository.findById(articleId)
			.filter(article -> article.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new CustomApiException(ErrorCode.ONLY_TEAM_MEMBER_CAN_VIEW));

		return new TeamArticleResponseDto(teamArticleRepository.save(teamArticle));
	}
}
