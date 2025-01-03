package com.sparta.sportify.dto.match.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class MatchesByDateResponseDto {
    public List<MatchByStadiumResponseDto> data;
}
