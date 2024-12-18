package com.sparta.sportify.dto.teamArticle.response;

import com.sparta.sportify.entity.teamArticle.TeamArticle;

import lombok.Getter;

@Getter
public class TeamArticleResponseDto {
	private String title;
	private String content;
	private Long userId;
	private Long teamId;

	public TeamArticleResponseDto(TeamArticle teamArticle) {
		this.title = teamArticle.getTitle();
		this.content = teamArticle.getContent();
		this.userId = teamArticle.getUser().getId();
		this.teamId = teamArticle.getTeam().getId();
	}
}
