package com.sparta.sportify.service.stadium;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.repository.StadiumRepository;

class StadiumServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@InjectMocks
	private StadiumService stadiumService;

	private StadiumCreateRequestDto stadiumCreateRequestDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000);
	}

	@Test
	@DisplayName("구장 생성 성공")
	void createStadium() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto);

		when(stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName()))
			.thenReturn(Optional.empty());
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		assertNotNull(stadiumService.CreateStadium(stadiumCreateRequestDto));
		verify(stadiumRepository, times(1)).save(any(Stadium.class));
	}


	@Test
	@DisplayName("구장 이름 중복")
	void existStadiumName() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto);

		when(stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName()))
			.thenReturn(Optional.of(new Stadium()));
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumService.CreateStadium(stadiumCreateRequestDto);
		});

		assertEquals("구장 이름이 이미 존재합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}
}