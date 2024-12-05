package com.sparta.sportify.service.stadium;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.repository.StadiumRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StadiumService {
	private final StadiumRepository stadiumRepository;

	public StadiumResponseDto CreateStadium(StadiumCreateRequestDto stadiumCreateRequestDto) {

		Optional<Stadium> stadiumName = stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName());

		if(stadiumName.isPresent()) {
			throw new IllegalArgumentException("구장 이름이 이미 존재합니다");
		}

		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto);

		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}
}
