package com.sparta.sportify.service.stadiumTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeResponseDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.StadiumStatus;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumService;
import com.sparta.sportify.service.StadiumTimeService;

class StadiumTimeServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@Mock
	private StadiumTimeRepository stadiumTimeRepository;

	@InjectMocks
	private StadiumService stadiumService;

	@InjectMocks
	private StadiumTimeService stadiumTimeService;

	private StadiumCreateRequestDto stadiumCreateRequestDto;
	private StadiumUpdateRequestDto stadiumUpdateRequestDto;

	private StadiumTimeRequestDto stadiumTimeRequestDto;

	private UserDetailsImpl userDetails;

	private Long stadiumId;
	private Long stadiumTimeId;
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		stadiumId = 1L;
		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000);
		stadiumUpdateRequestDto = new StadiumUpdateRequestDto("B구장", "서울", 6, 6, "넓고 좋은 경기장", 100000);

		stadiumTimeId = 1L;
		stadiumTimeRequestDto = new StadiumTimeRequestDto(
			Arrays.asList("10-12", "14-16", "16-18"),
			Arrays.asList("mon", "tue", "wed")
		);

		userDetails = mock(UserDetailsImpl.class);
		when(userDetails.getUsername()).thenReturn("testUser");
	}

	@Test
	@DisplayName("구장 운영시간 생성")
	void createStadiumTime() {
		Stadium stadium = Stadium.createOf(
			stadiumCreateRequestDto.getStadiumName(),
			stadiumCreateRequestDto.getLocation(),
			stadiumCreateRequestDto.getATeamCount(),
			stadiumCreateRequestDto.getBTeamCount(),
			stadiumCreateRequestDto.getDescription(),
			stadiumCreateRequestDto.getPrice(),
			userDetails);
		stadium.setId(stadiumId);
		stadium.setStatus(StadiumStatus.APPROVED);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		String cron = stadiumTimeService.convertToCronExpression(stadiumTimeRequestDto);
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadium);

		when(stadiumTimeRepository.save(any(StadiumTime.class))).thenReturn(stadiumTime);

		StadiumTimeResponseDto response = stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);

		assertNotNull(response);
		assertEquals(stadiumId, response.getStadiumId());
		assertTrue(response.getCronExpression().contains("10-12"));
		assertTrue(response.getCronExpression().contains("mon"));

		verify(stadiumRepository, times(1)).findById(stadiumId);
		verify(stadiumTimeRepository, times(1)).save(any(StadiumTime.class));
	}

	@Test
	@DisplayName("구장이 승인되지 않아 에러")
	void NotApprovedStadium() {
		Stadium stadium = Stadium.createOf(
			stadiumCreateRequestDto.getStadiumName(),
			stadiumCreateRequestDto.getLocation(),
			stadiumCreateRequestDto.getATeamCount(),
			stadiumCreateRequestDto.getBTeamCount(),
			stadiumCreateRequestDto.getDescription(),
			stadiumCreateRequestDto.getPrice(),
			userDetails);
		stadium.setId(stadiumId);
		//stadium.setStatus(StadiumStatus.APPROVED);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		String cron = stadiumTimeService.convertToCronExpression(stadiumTimeRequestDto);
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadium);

		when(stadiumTimeRepository.save(any(StadiumTime.class))).thenReturn(stadiumTime);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);
		});

		assertEquals("승인되지 않은 구장입니다", thrown.getMessage());

		verify(stadiumTimeRepository, times(0)).save(any(StadiumTime.class));
	}

	@Test
	@DisplayName("구장 시간이 이미 존재하여 에러")
	void existStadiumTime() {
		Stadium stadium = Stadium.createOf(
			stadiumCreateRequestDto.getStadiumName(),
			stadiumCreateRequestDto.getLocation(),
			stadiumCreateRequestDto.getATeamCount(),
			stadiumCreateRequestDto.getBTeamCount(),
			stadiumCreateRequestDto.getDescription(),
			stadiumCreateRequestDto.getPrice(),
			userDetails);
		stadium.setId(stadiumId);
		stadium.setStatus(StadiumStatus.APPROVED);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		String cron = stadiumTimeService.convertToCronExpression(stadiumTimeRequestDto);
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadium);

		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));
		when(stadiumTimeRepository.findByStadiumId(stadiumId)).thenReturn(Optional.of(stadiumTime));

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);
		});

		assertEquals("구장 시간이 이미 저장되었습니다", thrown.getMessage());

		verify(stadiumTimeRepository, times(0)).save(any(StadiumTime.class));
	}
}












