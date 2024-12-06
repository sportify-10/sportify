package com.sparta.sportify.service;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.Reservation;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.ReservationRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.cron.CronUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumTimeRepository stadiumTimeRepository;


    @Transactional
    public ReservationResponseDto reservationPersonal(ReservationRequestDto requestDto, UserDetailsImpl authUser) {

        StadiumTime stadiumTime = stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                ()->new RuntimeException("구장이 운영중이 아닙니다.")
        );

        if(!CronUtil.isCronDateAllowed(stadiumTime.getCron(),requestDto.getReservationDate(),requestDto.getTime())){
            throw new RuntimeException("구장 운영시간이 맞지 않습니다.");
        }

        if(reservationRepository.existsByUserAndMatchTimeAndReservationDate(authUser.getUser(),requestDto.getTime(),requestDto.getReservationDate())){
            throw new RuntimeException("이미 중복된 시간에 예약을 하였습니다.");
        }


        Match match = matchRepository.findByDateAndTime(requestDto.getReservationDate(),requestDto.getTime()).map(findMatch ->{
                    switch (requestDto.getTeamColor()){
                        case A -> findMatch.discountATeamCount(1);
                        case B -> findMatch.discountBTeamCount(1);
                        default -> throw new RuntimeException("잘못된 요청");
                    }
                    return matchRepository.save(findMatch);
                }).orElse(
                matchRepository.save(
                        Match.builder()
                            .date(requestDto.getReservationDate())
                            .time(requestDto.getTime())
                            .aTeamCount(stadiumTime.getStadium().getATeamCount())
                            .bTeamCount(stadiumTime.getStadium().getBTeamCount())
                            .stadiumTime(stadiumTime)
                            .build()
                )
        );

        Stadium stadium = stadiumTime.getStadium();

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                    .reservationDate(requestDto.getReservationDate())
                    .reservationDate(requestDto.getReservationDate())
                    .totalAmount(stadium.getPrice())
                    .status("예약중")
                    .teamColor(requestDto.getTeamColor())
                    .user(authUser.getUser())
                    .match(match)
                    .build());

        return new ReservationResponseDto(reservation.getId());
    }
}
