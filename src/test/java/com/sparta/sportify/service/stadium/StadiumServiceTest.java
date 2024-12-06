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
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.security.UserDetailsImpl;

class StadiumServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@InjectMocks
	private StadiumService stadiumService;

	private StadiumCreateRequestDto stadiumCreateRequestDto;
	private StadiumUpdateRequestDto stadiumUpdateRequestDto;

	private UserDetailsImpl userDetails;

	private Long stadiumId;
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		stadiumId = 1L;
		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000);
		stadiumUpdateRequestDto = new StadiumUpdateRequestDto("B구장", "서울", 6, 6, "넓고 좋은 경기장", 100000);

		userDetails = mock(UserDetailsImpl.class);

		User user = new User();
		user.setId(1L);
		when(userDetails.getUser()).thenReturn(user);

		Stadium stadium = new Stadium();
		stadium.setId(stadiumId);
		stadium.setUser(user);

		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));
	}

	@Test
	@DisplayName("구장 생성 성공")
	void createStadium() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto, userDetails);

		when(stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName()))
			.thenReturn(Optional.empty());
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		assertNotNull(stadiumService.createStadium(stadiumCreateRequestDto, userDetails));
		verify(stadiumRepository, times(1)).save(any(Stadium.class));
	}


	@Test
	@DisplayName("구장 이름 중복되어 에러")
	void existStadiumName() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto, userDetails);

		when(stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName()))
			.thenReturn(Optional.of(new Stadium()));
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumService.createStadium(stadiumCreateRequestDto, userDetails);
		});

		assertEquals("구장 이름이 이미 존재합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 정보 수정")
	void updateStadium() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto, userDetails);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		stadiumService.updateStadium(stadiumId, stadiumUpdateRequestDto, userDetails);

		assertEquals(stadiumUpdateRequestDto.getStadiumName(), stadium.getStadiumName());
		assertEquals(stadiumUpdateRequestDto.getLocation(), stadium.getLocation());
		assertEquals(stadiumUpdateRequestDto.getATeamCount(), stadium.getATeamCount());
		assertEquals(stadiumUpdateRequestDto.getBTeamCount(), stadium.getBTeamCount());
		assertEquals(stadiumUpdateRequestDto.getDescription(), stadium.getDescription());
		assertEquals(stadiumUpdateRequestDto.getPrice(), stadium.getPrice());
	}

	@Test
	@DisplayName("구장 존재하지 않아 에러")
	void notFoundStadium() {
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumService.updateStadium(stadiumId, stadiumUpdateRequestDto, userDetails);
		});

		assertEquals("구장이 존재하지 않습니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 주인이 아닌 사람의 수정 요청 시 에러")
	void notOwnerStadium() {
		UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
		User notOwner = new User();
		notOwner.setId(999L);

		// userDetails 모킹
		when(userDetails.getUser()).thenReturn(notOwner);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			stadiumService.updateStadium(stadiumId, stadiumUpdateRequestDto, userDetails);
		});

		assertEquals("자신의 구장만 수정 가능합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 삭제")
	void deleteStadium() {
		Stadium stadium = Stadium.createOf(stadiumCreateRequestDto, userDetails);
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		stadiumService.deleteStadium(stadiumId);

		assertNotNull(stadium.getDeletedAt());
	}
}