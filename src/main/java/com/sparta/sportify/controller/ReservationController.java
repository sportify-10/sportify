package com.sparta.sportify.controller;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ApiResult<ReservationResponseDto>> reservationsProcess(
            @RequestBody ReservationRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl authUser
    ) {
        if (requestDto.getTeamMemberIdList().isEmpty()) {
            return new ResponseEntity<>(
                    ApiResult.success(
                            "개인예약 성공",
                            reservationService.reservationPersonal(requestDto, authUser)
                    ),
                    HttpStatus.CREATED
            );
        } else {
            return new ResponseEntity<>(
                    ApiResult.success(
                            "개인예약 성공",
                            reservationService.reservationGroup(requestDto, authUser)
                    ),
                    HttpStatus.CREATED
            );
        }

    }

//    /reservations?page=0&size=10
    @GetMapping("/reservations")
    public ResponseEntity<ApiResult<Slice<ReservationFindResponseDto>>> findAllReservations(
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        return new ResponseEntity<>(
                ApiResult.success(
                        "개인예약 성공",
                        reservationService.findReservationsForInfiniteScroll(authUser,pageable)
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ApiResult<ReservationFindResponseDto>> findReservations(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetailsImpl authUser
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "개인예약 성공",
                        reservationService.findReservation(reservationId, authUser)
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<ApiResult<ReservationResponseDto>> deleteReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetailsImpl authUser
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "개인예약 성공",
                        reservationService.deleteReservation(reservationId, authUser)
                ),
                HttpStatus.OK
        );
    }
}
