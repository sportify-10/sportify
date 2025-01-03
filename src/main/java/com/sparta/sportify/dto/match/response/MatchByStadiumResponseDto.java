package com.sparta.sportify.dto.match.response;

import com.sparta.sportify.entity.matchResult.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class MatchByStadiumResponseDto {
    public Long stadiumId;
    public String stadiumName;
    public String stadiumDescription;
    public String stadiumLocation;
    public String startTime;
    public String endTime;
    public MatchStatus status;
}
