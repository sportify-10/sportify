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
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.team.TeamColor;
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

import java.util.Collections;
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

    private Match handleMatch(ReservationRequestDto requestDto, StadiumTime stadiumTime, List<User> users) {
        Match match = matchRepository.findByIdAndDateAndTime(
                requestDto.getStadiumTimeId(),
                requestDto.getReservationDate(),
                requestDto.getTime()
        ).map(existingMatch -> updateMatch(existingMatch, requestDto, users)).orElseGet(() -> createMatch(requestDto, stadiumTime, users));

        return matchRepository.save(match);
    }

    private Match updateMatch(Match existingMatch, ReservationRequestDto requestDto, List<User> users) {
        switch (requestDto.getTeamColor()) {
            case A -> existingMatch.discountATeamCount(users.size());
            case B -> existingMatch.discountBTeamCount(users.size());
        }
        return existingMatch;
    }

    private Match createMatch(ReservationRequestDto requestDto, StadiumTime stadiumTime, List<User> users) {
        int aTeamCount = stadiumTime.getStadium().getATeamCount() - users.size();
        int bTeamCount = stadiumTime.getStadium().getBTeamCount() - users.size();

        if (requestDto.getTeamColor() == TeamColor.A && aTeamCount < 0 || requestDto.getTeamColor() == TeamColor.B && bTeamCount < 0) {
            throw new CustomApiException(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM);
        }

        return Match.builder()
                .date(requestDto.getReservationDate())
                .time(requestDto.getTime())
                .aTeamCount(aTeamCount)
                .bTeamCount(bTeamCount)
                .stadiumTime(stadiumTime)
                .build();
    }

    private CashLog handleCashLog(UserDetailsImpl authUser, Long totalPrice) {
        CashLog cashLog = cashLogRepository.save(
                CashLog.builder()
                        .price(totalPrice)
                        .type(CashType.PAYMENT)
                        .user(authUser.getUser())
                        .build()
        );
        authUser.getUser().discountCash(totalPrice);
        userRepository.save(authUser.getUser());

        return cashLog;
    }

    private void handleCashLogReservationMapping(CashLog cashLog, List<Reservation> reservations) {
        reservations.forEach(reservation -> {
            CashLogReservationMappingId embeddedId = new CashLogReservationMappingId(cashLog.getId(), reservation.getId());

            cashLogReservationMappingRepository.save(
                    CashLogReservationMapping.builder()
                            .id(embeddedId)
                            .cashLog(cashLog)
                            .reservation(reservation)
                            .type(CashLogReservationMappingType.MINUS)
                            .build()
            );
        });
    }

    @RedissonLock(key = "'reservation-'.concat(#requestDto.getReservationDate().toString()).concat('/').concat(#requestDto.getStadiumTimeId().toString())")
    public ReservationResponseDto reservationPersonal(ReservationRequestDto requestDto, UserDetailsImpl authUser) {
        StadiumTime stadiumTime = getStadiumTime(requestDto);

        if (reservationRepository.existsByUserAndMatchTimeAndReservationDate(authUser.getUser(), requestDto.getTime(), requestDto.getReservationDate())) {
            throw new CustomApiException(ErrorCode.DUPLICATE_RESERVATION);
        }
        if (!CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime())) {
            throw new CustomApiException(ErrorCode.INVALID_OPERATION_TIME);
        }

        Match match = handleMatch(requestDto, stadiumTime, Collections.singletonList(authUser.getUser()));

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .reservationDate(requestDto.getReservationDate())
                        .totalAmount(stadiumTime.getStadium().getPrice())
                        .status(ReservationStatus.CONFIRMED)
                        .teamColor(requestDto.getTeamColor())
                        .user(authUser.getUser())
                        .match(match)
                        .build()
        );

        CashLog cashLog = handleCashLog(authUser, stadiumTime.getStadium().getPrice());

        handleCashLogReservationMapping(cashLog, Collections.singletonList(reservation));

        return new ReservationResponseDto(reservation.getId());
    }

    @RedissonLock(key = "'reservation-'.concat(#requestDto.getReservationDate().toString()).concat('/').concat(#requestDto.getStadiumTimeId().toString())")
    public ReservationResponseDto reservationGroup(ReservationRequestDto requestDto, UserDetailsImpl authUser) {
        StadiumTime stadiumTime = getStadiumTime(requestDto);

        List<User> users = getUsers(requestDto);

        Match match = handleMatch(requestDto, stadiumTime, users);

        Team team = teamRepository.findById(requestDto.getTeamId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND)
        );

        List<Reservation> reservations = users.stream()
                .map(user -> reservationRepository.save(
                        Reservation.builder()
                                .reservationDate(requestDto.getReservationDate())
                                .totalAmount(stadiumTime.getStadium().getPrice())
                                .status(ReservationStatus.CONFIRMED)
                                .teamColor(requestDto.getTeamColor())
                                .user(user)
                                .team(team)
                                .match(match)
                                .build()
                ))
                .toList();

        CashLog cashLog = handleCashLog(authUser, stadiumTime.getStadium().getPrice() * users.size());

        handleCashLogReservationMapping(cashLog, reservations);

        return new ReservationResponseDto(reservations.stream().map(Reservation::getId).toList());
    }

    private StadiumTime getStadiumTime(ReservationRequestDto requestDto) {
        return stadiumTimeRepository.findById(requestDto.getStadiumTimeId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.STADIUM_NOT_OPERATIONAL)
        );
    }

    private List<User> getUsers(ReservationRequestDto requestDto) {
        List<User> users = userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList());
        if (users.size() != requestDto.getTeamMemberIdList().size()) {
            throw new CustomApiException(ErrorCode.USER_INFO_INVALID);
        }
        if (reservationRepository.existsByUsersAndMatchTimeAndReservationDate(users, requestDto.getTime(), requestDto.getReservationDate())) {
            throw new CustomApiException(ErrorCode.DUPLICATE_RESERVATION);
        }
        return users;
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
                () -> new CustomApiException(ErrorCode.MATCH_NOT_FOUND)
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

        CashLogReservationMapping cashLogReservationMapping = cashLogReservationMappingRepository.findCashLogReservationMappingById(reservation.getId());

        CashLog cashlog = cashLogReservationMapping.getCashLog();

        cashlog.refund();
        cashLogRepository.save(cashlog);

        authUser.getUser().addCash(reservation.getTotalAmount());
        userRepository.save(authUser.getUser());


        return new ReservationResponseDto(reservation.getId());
    }
}