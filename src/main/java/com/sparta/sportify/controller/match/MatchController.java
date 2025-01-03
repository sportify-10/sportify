package com.sparta.sportify.controller.match;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.service.MatchService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<ApiResult<MatchesByDateResponseDto>> getMatchesByDate(
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                ApiResult.success("날짜별 매치 조회 성공", matchService.getMatchesByDate(date))
        );
    }

    @GetMapping("/{stadiumId}/{date}/{time}")
    public ResponseEntity<ApiResult<MatchDetailResponseDto>> getMatchByDateAndTime(
            @PathVariable Long stadiumId,
            @PathVariable LocalDate date,
            @PathVariable Integer time) {
        return ResponseEntity.ok(
                ApiResult.success("매치 조회 성공", matchService.getMatchByDateAndTime(stadiumId, date, time, LocalDateTime.now()))
        );
    }

    @PostMapping("/result/{matchId}")
    public ResponseEntity<ApiResult<MatchResultResponseDto>> createMatchResult(
            @Validated @RequestBody MatchResultRequestDto requestDto) {
        return ResponseEntity.ok(
                ApiResult.success("경기 결과 기록", matchService.createMatchResult(requestDto))
        );
    }

    @GetMapping("/result/{matchId}")
    public ResponseEntity<ApiResult<MatchResultResponseDto>> getMatchResult(@PathVariable Long matchId) {
        return ResponseEntity.ok(
                ApiResult.success("경기 결과 조회", matchService.getMatchResult(matchId))
        );
    }
}
