package com.sparta.sportify.dto.teamArticle.response;

import com.sparta.sportify.entity.teamArticle.TeamArticle;

public class TeamArticleResponseDto {
	private String title;
	private String content;
	private String userName;
	public TeamArticleResponseDto(TeamArticle teamArticle) {
		this.title = teamArticle.getTitle();
		this.content = teamArticle.getContent();
		this.userName = teamArticle.getUserName();
	}
}
