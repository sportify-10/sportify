package com.sparta.sportify.service.stadiumTimeService;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeCreateRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeCreateResponseDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.StadiumStatus;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StadiumTimeService {

	private final StadiumTimeRepository stadiumTimeRepository;
	private final StadiumRepository stadiumRepository;

	public StadiumTimeCreateResponseDto createStadiumTime(Long stadiumId, StadiumTimeCreateRequestDto stadiumTimeCreateRequestDto) {
		Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new IllegalArgumentException("구장이 존재하지 않습니다"));

		StadiumTime stadiumIdCheck = stadiumTimeRepository.findByStadiumId(stadiumId).orElse(null);

		//구장 타임 존재하면 예외처리
		if(stadiumIdCheck != null){
			throw new IllegalArgumentException("구장 시간이 이미 저장되었습니다");
		}

		//구장 승인 상태면 진행
		if(stadium.getStatus() != StadiumStatus.APPROVED) {
			throw new IllegalArgumentException("승인되지 않은 구장입니다");
		}

		String cron = convertToCronExpression(stadiumTimeCreateRequestDto);

		StadiumTime stadiumTime =  StadiumTime.createOf(cron, stadium);

		return new StadiumTimeCreateResponseDto(stadiumTimeRepository.save(stadiumTime), stadium);
	}

	public String convertToCronExpression(StadiumTimeCreateRequestDto stadiumTimeCreateRequestDto) {
		List<String> weeks = stadiumTimeCreateRequestDto.getWeeks();
		List<String> hours = stadiumTimeCreateRequestDto.getHours();

		StringBuilder cronBuilder = new StringBuilder();

		for(int i=0; i<hours.size(); i++){
			String[] hour = hours.get(i).split("-");
			String startHour = hour[0];
			String endHour = hour[1];

			if(i>0) {
				cronBuilder.append(",");
			}

			cronBuilder.append(startHour).append("-").append(endHour);
		}

		String hourString = cronBuilder.toString();

		String weekString = "";
		for (int i = 0; i < weeks.size(); i++) {
			weekString += weeks.get(i);
			if (i < weeks.size() - 1) { //마지막이면 쉼표 추가x
				weekString += ",";
			}
		}

		return String.format("0 0 %s ? * %s", hourString, weekString);
	}
}
