package com.sparta.sportify.service;

import com.sparta.sportify.annotation.RedissonLock;
import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.*;
import com.sparta.sportify.repository.*;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.cron.CronUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumTimeRepository stadiumTimeRepository;
    private final UserRepository userRepository;
    private final CashLogReservationMappingRepository cashLogReservationMappingRepository;
    private final CashLogRepository cashLogRepository;


    @RedissonLock(key="'reservation-'.concat(#requestDto.getReservationDate().toString()).concat('/').concat(#requestDto.getStadiumTimeId().toString())")
    public ReservationResponseDto reservationPersonal(ReservationRequestDto requestDto, UserDetailsImpl authUser) {
        StadiumTime stadiumTime = stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                () -> new RuntimeException("구장이 운영중이 아닙니다.")
        );

        if (!CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime())) {
            throw new RuntimeException("구장 운영시간이 맞지 않습니다.");
        }

        if (reservationRepository.existsByUserAndMatchTimeAndReservationDate(authUser.getUser(), requestDto.getTime(), requestDto.getReservationDate())) {
            throw new RuntimeException("이미 중복된 시간에 예약을 하였습니다.");
        }


        Match match = matchRepository.findByIdAndDateAndTime(
                requestDto.getStadiumTimeId(),
                requestDto.getReservationDate(),
                requestDto.getTime()
        ).map(findMatch -> {
            switch (requestDto.getTeamColor()) {
                case A -> findMatch.discountATeamCount(1);
                case B -> findMatch.discountBTeamCount(1);
                default -> throw new RuntimeException("잘못된 요청");
            }
            return matchRepository.save(findMatch);
        }).orElseGet(() -> {
            int aTeamCount = stadiumTime.getStadium().getATeamCount();
            int bTeamCount = stadiumTime.getStadium().getBTeamCount();
            switch (requestDto.getTeamColor()) {
                case A -> aTeamCount = aTeamCount - 1;
                case B -> bTeamCount = bTeamCount - 1;
                default -> throw new RuntimeException("잘못된 요청");
            }
            return matchRepository.save(
                    Match.builder()
                            .date(requestDto.getReservationDate())
                            .time(requestDto.getTime())
                            .aTeamCount(aTeamCount)
                            .bTeamCount(bTeamCount)
                            .stadiumTime(stadiumTime)
                            .build()
            );
        });

        Stadium stadium = stadiumTime.getStadium();

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .reservationDate(requestDto.getReservationDate())
                        .totalAmount(stadium.getPrice())
                        .status(ReservationStatus.CONFIRMED)
                        .teamColor(requestDto.getTeamColor())
                        .user(authUser.getUser())
                        .match(match)
                        .build());

        CashLog cashLog = cashLogRepository.save(
                CashLog.builder()
                        .price(stadium.getPrice())
                        .type(CashType.PAYMENT)
                        .user(authUser.getUser())
                        .build()
        );

        CashLogReservationMappingId embeddedId = new CashLogReservationMappingId();
        embeddedId.setCashTransactionId(cashLog.getId());
        embeddedId.setReservationId(reservation.getId());

        cashLogReservationMappingRepository.save(
                CashLogReservationMapping.builder()
                        .id(embeddedId)
                        .cashLog(cashLog)
                        .reservation(reservation)
                        .type(CashLogReservationMappingType.MINUS)
                        .build()
        );

        authUser.getUser().setCash(authUser.getUser().getCash() - stadium.getPrice());
        userRepository.save(authUser.getUser());

        return new ReservationResponseDto(reservation.getId());
    }

    @RedissonLock(key="'reservation-'.concat(#reqeustDto.getReservationDate()).concat('/').concat(#requestDto.getStadiumTimeId())")
    public ReservationResponseDto reservationGroup(ReservationRequestDto requestDto, UserDetailsImpl authUser) {

        StadiumTime stadiumTime = stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                () -> new RuntimeException("구장이 운영중이 아닙니다.")
        );

        if (!CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime())) {
            throw new RuntimeException("구장 운영시간이 맞지 않습니다.");
        }

        List<User> users = userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList());
        if (users.size() != requestDto.getTeamMemberIdList().size()) {
            throw new RuntimeException("유저 정보가 잘못됨 ");
        }

        if (reservationRepository.existsByUsersAndMatchTimeAndReservationDate(users, requestDto.getTime(), requestDto.getReservationDate())) {
            throw new RuntimeException("이미 중복된 시간에 예약을 하였습니다.");
        }


        Team team = teamRepository.findById(requestDto.getTeamId()).orElseThrow(
                () -> new RuntimeException("팀을 찾을 수 없습니다")
        );


        Match match = matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()).map(findMatch -> {
            switch (requestDto.getTeamColor()) {
                case A -> {
                    if (findMatch.getATeamCount() < users.size()) {
                        throw new RuntimeException("요청한 인원수보다 남은 자리수가 적습니다.");
                    }
                    findMatch.discountATeamCount(users.size());
                }
                case B -> {
                    if (findMatch.getBTeamCount() < users.size()) {
                        throw new RuntimeException("요청한 인원수보다 남은 자리수가 적습니다.");
                    }
                    findMatch.discountBTeamCount(users.size());
                }
                default -> throw new RuntimeException("잘못된 요청");
            }
            return matchRepository.save(findMatch);
        }).orElseGet(() -> {
            int aTeamCount = stadiumTime.getStadium().getATeamCount();
            int bTeamCount = stadiumTime.getStadium().getBTeamCount();
            switch (requestDto.getTeamColor()) {
                case A -> {
                    if (aTeamCount < users.size()) {
                        throw new RuntimeException("요청한 인원수보다 남은 자리수가 적습니다.");
                    }
                    aTeamCount = aTeamCount - users.size();
                }
                case B -> {
                    if (bTeamCount < users.size()) {
                        throw new RuntimeException("요청한 인원수보다 남은 자리수가 적습니다.");
                    }
                    bTeamCount = bTeamCount - users.size();
                }
                default -> throw new RuntimeException("잘못된 요청");
            }
            return matchRepository.save(
                    Match.builder()
                            .date(requestDto.getReservationDate())
                            .time(requestDto.getTime())
                            .aTeamCount(aTeamCount)
                            .bTeamCount(bTeamCount)
                            .stadiumTime(stadiumTime)
                            .build()
            );
        });

        Stadium stadium = stadiumTime.getStadium();


        List<Reservation> reservations = users.stream()
                .map(user -> Reservation.builder()
                        .reservationDate(requestDto.getReservationDate())
                        .totalAmount(stadium.getPrice())
                        .status(ReservationStatus.CONFIRMED)
                        .teamColor(requestDto.getTeamColor())
                        .user(user)
                        .team(team)
                        .match(match)
                        .build())
                .map(reservationRepository::save)
                .toList();

        CashLog cashLog = cashLogRepository.save(
                CashLog.builder()
                        .price(stadium.getPrice() * users.size())
                        .type(CashType.PAYMENT)
                        .user(authUser.getUser())
                        .build()
        );

        reservations.forEach(reservation -> {
                    CashLogReservationMappingId embeddedId = new CashLogReservationMappingId();
                    embeddedId.setCashTransactionId(cashLog.getId());
                    embeddedId.setReservationId(reservation.getId());

                    cashLogReservationMappingRepository.save(
                            CashLogReservationMapping.builder()
                                    .id(embeddedId)
                                    .cashLog(cashLog)
                                    .reservation(reservation)
                                    .type(CashLogReservationMappingType.MINUS)
                                    .build()
                    );
                }
        );

        authUser.getUser().setCash(authUser.getUser().getCash() - (stadium.getPrice() * users.size()));
        userRepository.save(authUser.getUser());

        return new ReservationResponseDto(reservations.stream().map(Reservation::getId).toList());
    }


    @Transactional
    public ReservationFindResponseDto findReservation(Long reservationId, UserDetailsImpl authUser) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("찾을 수 없는 예약 ID입니다.")
        );

        if (reservation.getUser().getId() != authUser.getUser().getId()) {
            throw new RuntimeException("해당 유저 정보가 다릅니다");
        }

        return new ReservationFindResponseDto(reservation);
    }


    @Transactional
    public Slice<ReservationFindResponseDto> findReservationsForInfiniteScroll(
            UserDetailsImpl authUser, Pageable pageable) {
        Slice<Reservation> reservations = reservationRepository.findByUserIdOrderByIdDesc(authUser.getUser().getId(), pageable);

        return reservations.map(ReservationFindResponseDto::new);
    }

    @Transactional
    public ReservationResponseDto deleteReservation(Long reservationId, UserDetailsImpl authUser) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new RuntimeException("예약ID를 찾을 수 없습니다.")
        );
        if (reservation.getUser().getId() != authUser.getUser().getId()) {
            throw new RuntimeException("해당 유저 정보가 다릅니다.");
        }

        reservation.markAsDeleted();
        reservationRepository.save(reservation);

        Match match = matchRepository.findById(reservation.getMatch().getId()).orElseThrow(
                () -> new RuntimeException("해당 유저 정보가 다릅니다.")
        );

        switch (reservation.getTeamColor()) {
            case A -> {
                match.addATeamCount(1);
            }
            case B -> {
                match.addBTeamCount(1);
            }
        }
        matchRepository.save(match);

        return new ReservationResponseDto(reservation.getId());
    }

}
