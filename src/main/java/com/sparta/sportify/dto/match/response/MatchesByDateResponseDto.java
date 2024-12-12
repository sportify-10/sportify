package com.sparta.sportify.dto.match.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MatchesByDateResponseDto {
	private final List<MatchByStadiumResponseDto> data;
}
