package com.sparta.sportify.service;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.cron.CronUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StadiumTimeService {

    private final StadiumTimeRepository stadiumTimeRepository;
    private final StadiumRepository stadiumRepository;

    public StadiumTimeResponseDto createStadiumTime(Long stadiumId, StadiumTimeRequestDto stadiumTimeRequestDto) {
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new IllegalArgumentException("구장이 존재하지 않습니다")
        );

        StadiumTime stadiumIdCheck = stadiumTimeRepository.findByStadiumId(stadiumId).orElse(null);

        //구장 타임 존재하면 예외처리
        if (stadiumIdCheck != null) {
            throw new IllegalArgumentException("구장 시간이 이미 저장되었습니다");
        }

        //구장 승인 상태면 진행
        if (stadium.getStatus() != StadiumStatus.APPROVED) {
            throw new IllegalArgumentException("승인되지 않은 구장입니다");
        }

        String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(), stadiumTimeRequestDto.getHours());

        StadiumTime stadiumTime = StadiumTime.builder()
                .cron(cron)
                .stadium(stadium)
                .build();
        return new StadiumTimeResponseDto(stadiumTimeRepository.save(stadiumTime), stadium);
    }


    public StadiumTimeResponseDto updateStadiumTime(Long stadiumTimeId, StadiumTimeRequestDto stadiumTimeRequestDto, UserDetailsImpl userDetails) {
        StadiumTime stadiumTime = stadiumTimeRepository.findById(stadiumTimeId).orElseThrow(
                () -> new IllegalArgumentException("저장된 구장 시간이 없습니다")
        );

        if (!stadiumTime.getStadium().getUser().getId().equals(userDetails.getUser().getId())) {
            throw new IllegalArgumentException("자신의 구장만 수정 가능합니다");
        }

        String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(), stadiumTimeRequestDto.getHours());

        stadiumTime.updateOf(cron);

        return new StadiumTimeResponseDto(stadiumTimeRepository.save(stadiumTime), stadiumTime.getStadium());
    }
}
