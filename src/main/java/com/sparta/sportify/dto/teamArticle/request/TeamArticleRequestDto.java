package com.sparta.sportify.dto.teamArticle.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamArticleRequestDto {
	private String title;
	private String content;
}
