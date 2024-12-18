package com.sparta.sportify.service.teamArticle;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.teamArticle.request.TeamArticleRequestDto;
import com.sparta.sportify.dto.teamArticle.response.TeamArticleResponseDto;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.teamArticle.TeamArticle;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.teamArticle.TeamArticleRepository;
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
		Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다"));

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new IllegalArgumentException("팀 멤버만 작성 가능합니다"));

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
			.orElseThrow(() -> new IllegalArgumentException("팀 멤버만 조회 가능합니다"));

		Page<TeamArticle> teamArticle = teamArticleRepository.findAllByTeamId(teamId, pageable);
		if (teamArticle.getContent().isEmpty()) {
			throw new IllegalArgumentException("게시글이 없습니다");
		}

		return teamArticle.map(TeamArticleResponseDto::new);
	}

	@Transactional
	public TeamArticleResponseDto updatePost(Long articleId, TeamArticleRequestDto teamArticleRequestDto,
		UserDetailsImpl userDetails) {
		TeamArticle teamArticle = teamArticleRepository.findById(articleId)
			.orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다"));

		if(teamArticle.getUser().getId() != userDetails.getUser().getId()){
			throw new IllegalArgumentException("자신의 게시물만 수정 가능합니다");
		}

		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamArticle.getTeam().getId())
			.filter(member -> member.getStatus() == TeamMember.Status.APPROVED)
			.filter(member -> member.getDeletedAt() == null)
			.orElseThrow(() -> new IllegalArgumentException("팀 멤버만 작성 가능합니다"));

		teamArticle.updateOf(teamArticleRequestDto.getTitle(), teamArticleRequestDto.getContent(),
			userDetails.getUser(), teamArticle.getTeam());

		return new TeamArticleResponseDto(teamArticleRepository.save(teamArticle));
	}
}
