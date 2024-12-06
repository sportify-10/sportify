package com.sparta.sportify.dto.reservation.request;

import com.sparta.sportify.entity.TeamColor;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequestDto {


    long teamId;

    @NotBlank(message = "구장에 대한 시간 선택은 필수 항목입니다..")
    Long stadiumTimeId;

    @NotBlank(message = "날짜 데이터는 필수 항목입니다.")
    LocalDate reservationDate;

    @NotBlank(message = "팀 선택은 필수 항목입니다. (A/B).")
    TeamColor teamColor;

    List<Integer> teamMemberIdList;

    @NotBlank(message = "시간 선택은 필수 사항입니다..")
    int time;

}
