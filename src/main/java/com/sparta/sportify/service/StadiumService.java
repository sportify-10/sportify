package com.sparta.sportify.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumMatchResponseDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.ReservationStatus;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.ReservationRepository;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StadiumService {
	private final StadiumRepository stadiumRepository;
	private final MatchRepository matchRepository;
	private final ReservationRepository reservationRepository;

	public StadiumResponseDto createStadium(StadiumCreateRequestDto stadiumCreateRequestDto, UserDetailsImpl userDetails) {
		Optional<Stadium> stadiumName = stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName());

		if(stadiumName.isPresent()) {
			throw new IllegalArgumentException("구장 이름이 이미 존재합니다");
		}

		Stadium stadium = Stadium.createOf(
			stadiumCreateRequestDto.getStadiumName(),
			stadiumCreateRequestDto.getLocation(),
			stadiumCreateRequestDto.getATeamCount(),
			stadiumCreateRequestDto.getBTeamCount(),
			stadiumCreateRequestDto.getDescription(),
			stadiumCreateRequestDto.getPrice(),
			userDetails
		);
		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}

	public List<StadiumResponseDto> getStadiums(UserDetailsImpl userDetails, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Stadium> stadiums = stadiumRepository.findAllByUserId(userDetails.getUser().getId(), pageable);

		return stadiums.getContent().stream()
			.map(StadiumResponseDto::new)
			.collect(Collectors.toList());
	}

	@Transactional
	public StadiumResponseDto updateStadium(Long stadiumId, StadiumUpdateRequestDto stadiumUpdateRequestDto, UserDetailsImpl userDetails) {
		Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new IllegalArgumentException("구장이 존재하지 않습니다"));

		if(!userDetails.getUser().getId().equals(stadium.getUser().getId())) {
			throw new IllegalArgumentException("자신의 구장만 수정 가능합니다");
		}

		stadium.updateOf(
			stadiumUpdateRequestDto.getStadiumName(),
			stadiumUpdateRequestDto.getLocation(),
			stadiumUpdateRequestDto.getATeamCount(),
			stadiumUpdateRequestDto.getBTeamCount(),
			stadiumUpdateRequestDto.getDescription(),
			stadiumUpdateRequestDto.getPrice()
		);

		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}

	@Transactional
	public StadiumResponseDto deleteStadium(Long stadiumId, UserDetailsImpl userDetails) {
		Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new IllegalArgumentException("구장이 존재하지 않습니다"));

		if(!userDetails.getUser().getId().equals(stadium.getUser().getId())) {
			throw new IllegalArgumentException("자신의 구장만 삭제 가능합니다");
		}

		stadium.deleteOf();
		return new StadiumResponseDto(stadiumRepository.save(stadium));
	}

	public List<StadiumMatchResponseDto> findMatchesByStadium(Long stadiumId) {
		stadiumRepository.findById(stadiumId).orElseThrow(()->new IllegalArgumentException("구장이 존재하지 않습니다"));

		////스타디움 아이디로 스타디움 타임을 가진 매치 조회
		List<Match> matches = matchRepository.findByStadiumTime_Stadium_Id(stadiumId);

		return matches.stream().map(match -> {
			//매치별 totalAmount 계산
			//reservation에서 matchId를 가진 totalAmount 총합
			Integer totalAmount = reservationRepository.findTotalAmountByMatchId(match.getId(), ReservationStatus.CONFIRMED);

			return new StadiumMatchResponseDto(
				match.getStadiumTime().getStadium().getId(),
				match.getStadiumTime().getStadium().getStadiumName(),
				match.getDate(),
				String.format("%02d:%02d", match.getTime() / 100, match.getTime() % 100), //시간 HH:MM 형식으로
				totalAmount, //매치별 예약금
				match.getATeamCount(),
				match.getBTeamCount()
			);
		}).collect(Collectors.toList());
	}
}
