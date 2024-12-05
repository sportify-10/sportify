package com.sparta.sportify.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.match.MatchResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

	@Autowired
	private MatchRepository matchRepository;

	public MatchResponseDto getMatchById(Long id) {
		Optional<Match> matchOptional = matchRepository.findById(id);
		if (matchOptional.isPresent()) {
			Match match = matchOptional.get();
			return new MatchResponseDto(
				match.getId(),
				match.getDate(),
				match.getTime(),
				match.getaTeamCount(),
				match.getbTeamCount()
			);
		} else {
			throw new IllegalArgumentException("유효하지 않은 경기 ID입니다.");
		}
	}

}