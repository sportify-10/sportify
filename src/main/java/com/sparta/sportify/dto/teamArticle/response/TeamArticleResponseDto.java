package com.sparta.sportify.dto.teamArticle.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.teamArticle.TeamArticle;

import lombok.Getter;

@Getter
public class TeamArticleResponseDto {
	private String title;
	private String content;
	private Long userId;
	private Long teamId;
	private LocalDateTime createAt;


	private String userName;
	public TeamArticleResponseDto(TeamArticle teamArticle) {
		this.title = teamArticle.getTitle();
		this.content = teamArticle.getContent();
		this.userId = teamArticle.getUser().getId();
		this.teamId = teamArticle.getTeam().getId();
		this.createAt = teamArticle.getCreateAt();
		this.userName = teamArticle.getUserName();
	}
}
