package com.sparta.sportify.dto.match;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MatchRequestDto {
	private Long id;

	public MatchRequestDto(Long id) {
		this.id = id;
	}
}