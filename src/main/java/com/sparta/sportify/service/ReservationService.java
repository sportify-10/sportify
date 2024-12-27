package com.sparta.sportify.service;

import com.sparta.sportify.annotation.RedissonLock;
import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.cashLog.*;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.Reservation;
import com.sparta.sportify.entity.reservation.ReservationStatus;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
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


    @RedissonLock(key = "'reservation-'.concat(#requestDto.getReservationDate().toString()).concat('/').concat(#requestDto.getStadiumTimeId().toString())")
    public ReservationResponseDto reservationPersonal(ReservationRequestDto requestDto, UserDetailsImpl authUser) {
        StadiumTime stadiumTime = stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.STADIUM_NOT_OPERATIONAL)
        );

        if (!CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime())) {
            throw new CustomApiException(ErrorCode.INVALID_OPERATION_TIME);
        }

        if (reservationRepository.existsByUserAndMatchTimeAndReservationDate(authUser.getUser(), requestDto.getTime(), requestDto.getReservationDate())) {
            throw new CustomApiException(ErrorCode.DUPLICATE_RESERVATION);
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

        authUser.getUser().discountCash(stadium.getPrice());
        userRepository.save(authUser.getUser());

        return new ReservationResponseDto(reservation.getId());
    }

    @RedissonLock(key = "'reservation-'.concat(#reqeustDto.getReservationDate()).concat('/').concat(#requestDto.getStadiumTimeId())")
    public ReservationResponseDto reservationGroup(ReservationRequestDto requestDto, UserDetailsImpl authUser) {

        StadiumTime stadiumTime = stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.STADIUM_NOT_OPERATIONAL)
        );

        if (!CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime())) {
            throw new CustomApiException(ErrorCode.INVALID_OPERATION_TIME);
        }

        List<User> users = userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList());
        if (users.size() != requestDto.getTeamMemberIdList().size()) {
            throw new CustomApiException(ErrorCode.USER_INFO_INVALID);
        }

        if (reservationRepository.existsByUsersAndMatchTimeAndReservationDate(users, requestDto.getTime(), requestDto.getReservationDate())) {
            throw new CustomApiException(ErrorCode.DUPLICATE_RESERVATION);
        }


        Team team = teamRepository.findById(requestDto.getTeamId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND)
        );


        Match match = matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()).map(findMatch -> {
            switch (requestDto.getTeamColor()) {
                case A -> {
                    if (findMatch.getATeamCount() < users.size()) {
                        throw new CustomApiException(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM);
                    }
                    findMatch.discountATeamCount(users.size());
                }
                case B -> {
                    if (findMatch.getBTeamCount() < users.size()) {
                        throw new CustomApiException(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM);
                    }
                    findMatch.discountBTeamCount(users.size());
                }
                default -> throw new CustomApiException(ErrorCode.INVALID_REQUEST);
            }
            return matchRepository.save(findMatch);
        }).orElseGet(() -> {
            int aTeamCount = stadiumTime.getStadium().getATeamCount();
            int bTeamCount = stadiumTime.getStadium().getBTeamCount();
            switch (requestDto.getTeamColor()) {
                case A -> {
                    if (aTeamCount < users.size()) {
                        throw new CustomApiException(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM);
                    }
                    aTeamCount = aTeamCount - users.size();
                }
                case B -> {
                    if (bTeamCount < users.size()) {
                        throw new CustomApiException(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM);
                    }
                    bTeamCount = bTeamCount - users.size();
                }
                default -> throw new CustomApiException(ErrorCode.INVALID_REQUEST);
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

        authUser.getUser().discountCash(stadium.getPrice() * users.size());
        userRepository.save(authUser.getUser());

        return new ReservationResponseDto(reservations.stream().map(Reservation::getId).toList());
    }


    @Transactional
    public ReservationFindResponseDto findReservation(Long reservationId, UserDetailsImpl authUser) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new CustomApiException(ErrorCode.RESERVATION_NOT_FOUND)
        );

        if (reservation.getUser().getId() != authUser.getUser().getId()) {
            throw new CustomApiException(ErrorCode.USER_INFO_MISMATCH);
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
                () -> new CustomApiException(ErrorCode.RESERVATION_NOT_FOUND)
        );
        if (reservation.getUser().getId() != authUser.getUser().getId()) {
            throw new CustomApiException(ErrorCode.USER_INFO_MISMATCH);
        }

        reservation.markAsDeleted();
        reservationRepository.save(reservation);

        Match match = matchRepository.findById(reservation.getMatch().getId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_INFO_MISMATCH)
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
