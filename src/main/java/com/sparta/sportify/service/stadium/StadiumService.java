package com.sparta.sportify.service.stadium;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StadiumService {
	private final StadiumRepository stadiumRepository;

	public StadiumResponseDto createStadium(StadiumCreateRequestDto stadiumCreateRequestDto, UserDetailsImpl userDetails) {
		Optional<Stadium> stadiumName = stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName());

		if(stadiumName.isPresent()) {
			throw new IllegalArgumentException("구장 이름이 이미 존재합니다");
		}

		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto, userDetails);

		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}

	public StadiumResponseDto updateStadium(Long stadiumId, StadiumUpdateRequestDto stadiumUpdateRequestDto, UserDetailsImpl userDetails) {
		Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new IllegalArgumentException("구장이 존재하지 않습니다"));

		if(!userDetails.getUser().getId().equals(stadium.getUser().getId())) {
			throw new IllegalArgumentException("자신의 구장만 수정 가능합니다");
		}

		stadium.updateOf(stadiumUpdateRequestDto);

		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}

	public StadiumResponseDto deleteStadium(Long stadiumId) {
		Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new IllegalArgumentException("구장이 존재하지 않습니다"));

		//구장 자기껀지 확인

		stadium.deleteOf();
		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}
}
