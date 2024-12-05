package com.sparta.sportify.controller;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return new ResponseEntity<>(
                ApiResult.success(
                        reservationService.reservationPersonal(requestDto,authUser)
                ),
                HttpStatus.CREATED
        );
    }


}
