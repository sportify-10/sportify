package com.sparta.sportify.dto.reservation.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReservationRequestDto {


    long teamId;

    @NonNull
    Long stadiumTimeId;

    @NonNull
    LocalDate reservationDate;

    @NonNull
    char teamColor;

    List<Integer> teamMemberIdList;

    @NonNull
    int time;

}
