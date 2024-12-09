package com.sparta.sportify.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.MatchResult;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

	private final MatchResultRepository matchResultRepository;

	private final MatchRepository matchRepository;

	@Transactional
	public MatchResultResponseDto createMatchResult(MatchResultRequestDto requestDto) {
		Match match = matchRepository.findById(requestDto.getMatchId())
			.orElseThrow(() -> new EntityNotFoundException("경기를 찾을 수 없습니다."));

		MatchResult matchResult = new MatchResult();
		matchResult.setTeamAScore(requestDto.getTeamAScore());
		matchResult.setTeamBScore(requestDto.getTeamBScore());
		matchResult.setMatch(match);
		matchResult.setMatchDate(LocalDate.now());

		MatchResult savedResult = matchResultRepository.save(matchResult);
		return new MatchResultResponseDto(
			savedResult.getId(),
			savedResult.getTeamAScore(),
			savedResult.getTeamBScore(),
			savedResult.getMatchStatus(),
			savedResult.getMatchDate()
		);
	}
}