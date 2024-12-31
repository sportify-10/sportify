package com.sparta.sportify.service.stadiumTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumTimeService;
import com.sparta.sportify.util.cron.CronUtil;

class StadiumTimeServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@Mock
	private StadiumTimeRepository stadiumTimeRepository;

	@InjectMocks
	private StadiumTimeService stadiumTimeService;

	private StadiumCreateRequestDto stadiumCreateRequestDto;
	private StadiumUpdateRequestDto stadiumUpdateRequestDto;

	private StadiumTimeRequestDto stadiumTimeRequestDto;

	private UserDetailsImpl userDetails;
	private UserDetailsImpl notUserDetails;

	private Long stadiumId;
	private Long stadiumTimeId;
	private StadiumTime stadiumTime;

	private Stadium stadium;
	private Stadium stadiumPending;
	private User user;
	private User notOwner;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		stadiumId = 1L;
		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000L);
		stadiumUpdateRequestDto = new StadiumUpdateRequestDto("B구장", "서울", 6, 6, "넓고 좋은 경기장", 100000L);

		stadiumTimeId = 1L;
		stadiumTimeRequestDto = new StadiumTimeRequestDto(
			Arrays.asList("10-12", "14-16", "16-18"),
			Arrays.asList("mon", "tue", "wed")
		);

		user = User.builder()
			.id(1L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L)
			.build();
		userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);

		//구장 주인이 아닌 객체
		notOwner = User.builder()
			.id(999L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L)
			.build();
		notUserDetails = new UserDetailsImpl(notOwner.getName(), notOwner.getRole(), notOwner);

		stadium = Stadium.builder()
			.id(1L)
			.stadiumName("Dream Stadium")
			.location("Seoul, South Korea")
			.aTeamCount(5)
			.bTeamCount(5)
			.description("A fantastic stadium for sports events.")
			.price(100000L)
			.status(StadiumStatus.APPROVED)
			.deletedAt(null)
			.user(user)
			.build();
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		stadiumPending = Stadium.builder()
			.id(1L)
			.stadiumName("Dream Stadium")
			.location("Seoul, South Korea")
			.aTeamCount(5)
			.bTeamCount(5)
			.description("A fantastic stadium for sports events.")
			.price(100000L)
			.status(StadiumStatus.PENDING)
			.deletedAt(null)
			.user(user)
			.build();
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		stadiumTime = StadiumTime.builder()
			.id(1L)
			.cron("0 0 10-12,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN")
			.stadium(stadium)
			.build();
		when(stadiumTimeRepository.save(any(StadiumTime.class))).thenReturn(stadiumTime);
		when(stadiumTimeRepository.findById(stadiumTime.getId())).thenReturn(Optional.of(stadiumTime));
	}

	@Test
	@DisplayName("구장 운영시간 생성")
	void createStadiumTime() {
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(),
			stadiumTimeRequestDto.getHours());
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadium);

		when(stadiumTimeRepository.save(any(StadiumTime.class))).thenReturn(stadiumTime);

		StadiumTimeResponseDto response = stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);

		assertNotNull(response);
		assertEquals(stadiumId, response.getStadiumId());
		assertTrue(response.getCronExpression().contains("10-12"));
		assertTrue(response.getCronExpression().contains("MON"));

		verify(stadiumRepository, times(1)).findById(stadiumId);
		verify(stadiumTimeRepository, times(1)).save(any(StadiumTime.class));
	}

	@Test
	@DisplayName("구장이 승인되지 않아 에러")
	void NotApprovedStadium() {
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadiumPending));

		String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(),
			stadiumTimeRequestDto.getHours());
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadiumPending);

		when(stadiumTimeRepository.save(any(StadiumTime.class))).thenReturn(stadiumTime);

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);
		});

		assertEquals("승인되지 않은 구장입니다", thrown.getMessage());

		verify(stadiumTimeRepository, times(0)).save(any(StadiumTime.class));
	}

	@Test
	@DisplayName("구장 시간이 이미 존재하여 에러")
	void existStadiumTime() {
		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));

		String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(),
			stadiumTimeRequestDto.getHours());
		StadiumTime stadiumTime = StadiumTime.createOf(cron, stadium);

		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(stadium));
		when(stadiumTimeRepository.findByStadiumId(stadiumId)).thenReturn(Optional.of(stadiumTime));

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);
		});

		assertEquals("구장 시간이 이미 저장되었습니다", thrown.getMessage());

		verify(stadiumTimeRepository, times(0)).save(any(StadiumTime.class));
	}

	@Test
	@DisplayName("저장된 구장이 없어 에러메시지")
	void createStadiumTime_noStadium() {
		Long stadiumId = 999L;

		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeRequestDto);
		});

		assertEquals("구장이 존재하지 않습니다", thrown.getMessage());
	}

	@Test
	@DisplayName("구장 시간 업데이트 성공")
	void updateStadiumTime() {

		stadiumTimeService.updateStadiumTime(stadiumTimeId, stadiumTimeRequestDto, userDetails);

		String cron = CronUtil.convertToCronExpression(stadiumTimeRequestDto.getWeeks(),
			stadiumTimeRequestDto.getHours());

		assertEquals(cron, stadiumTime.getCron());
	}

	@Test
	@DisplayName("저장된 구장 시간이 없어서 실패")
	void updateStadiumTime_noStadiumTime() {
		Long stadiumTimeId = 999L;

		when(stadiumTimeRepository.findById(stadiumTimeId)).thenReturn(Optional.empty());

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumTimeService.updateStadiumTime(stadiumTimeId, stadiumTimeRequestDto, userDetails);
		});

		assertEquals(ErrorCode.NO_STADIUM_TIME_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("다른 사람의 구장 시간 정보 수정 시도 에러")
	void updateStadiumTime_notOwner() {
		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumTimeService.updateStadiumTime(stadiumTime.getId(), stadiumTimeRequestDto, notUserDetails);
		});

		assertEquals(ErrorCode.ONLY_OWN_STADIUM_CAN_BE_MODIFIED, thrown.getErrorCode());
	}
}












