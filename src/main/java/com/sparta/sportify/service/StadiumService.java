package com.sparta.sportify.service;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumMatchResponseDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.ReservationStatus;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final MatchRepository matchRepository;


    public StadiumResponseDto createStadium(StadiumCreateRequestDto stadiumCreateRequestDto,
                                            UserDetailsImpl userDetails) {
        Optional<Stadium> stadiumName = stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName());

        if (stadiumName.isPresent()) {
            throw new CustomApiException(ErrorCode.STADIUM_NAME_ALREADY_EXISTS);
        }


        if (stadiumName.isPresent()) {
            throw new IllegalArgumentException("구장 이름이 이미 존재합니다");
        }
        Stadium stadium = Stadium.builder()
                .stadiumName(stadiumCreateRequestDto.getStadiumName())
                .location(stadiumCreateRequestDto.getLocation())
                .aTeamCount(stadiumCreateRequestDto.getATeamCount())
                .bTeamCount(stadiumCreateRequestDto.getBTeamCount())
                .description(stadiumCreateRequestDto.getDescription())
                .price(stadiumCreateRequestDto.getPrice())
                .user(userDetails.getUser())
                .status(StadiumStatus.APPROVED)
                .build();

        return new StadiumResponseDto(stadiumRepository.save(stadium));
    }

    public Page<StadiumResponseDto> getStadiums(UserDetailsImpl userDetails, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Stadium> stadiums = stadiumRepository.findAllByUserId(userDetails.getUser().getId(), pageable);
        if (stadiums.isEmpty()) {
            throw new CustomApiException(ErrorCode.NO_REGISTERED_STADIUM);
        }

        return stadiums.map(StadiumResponseDto::new);
    }

    @Transactional
    public StadiumResponseDto updateStadium(Long stadiumId, StadiumUpdateRequestDto stadiumUpdateRequestDto,
                                            UserDetailsImpl userDetails) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new CustomApiException(ErrorCode.NO_STADIUM_FOUND)
        );

        if (!userDetails.getUser().getId().equals(stadium.getUser().getId())) {
            throw new CustomApiException(ErrorCode.ONLY_OWN_STADIUM_CAN_BE_MODIFIED);
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
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.NO_STADIUM_FOUND));

        if (!userDetails.getUser().getId().equals(stadium.getUser().getId())) {
            throw new CustomApiException(ErrorCode.ONLY_OWN_STADIUM_CAN_BE_DELETED);
        }

        stadium.deleteOf();
        return new StadiumResponseDto(stadiumRepository.save(stadium));
    }

    public Page<StadiumMatchResponseDto> findMatchesByStadium(Long stadiumId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new CustomApiException(ErrorCode.NO_STADIUM_FOUND)
        );

        Page<Object[]> result = matchRepository.findMatchesWithTotalAmountByStadiumId(stadiumId,
                ReservationStatus.CONFIRMED, pageable);

        return result.map(objects -> {
            Match match = (Match) objects[0];

            Integer totalAmount = 0;
            if (objects[1] != null) {
                totalAmount = ((Long) objects[1]).intValue();
            }

            return new StadiumMatchResponseDto(
                    match.getStadiumTime().getStadium().getId(),
                    match.getStadiumTime().getStadium().getStadiumName(),
                    match.getDate(),
                    String.format("%02d:%02d", match.getTime(), 0), //시간 HH:MM 형식으로
                    totalAmount, //매치별 예약금
                    match.getATeamCount(),
                    match.getBTeamCount()
            );
        });
    }
}
