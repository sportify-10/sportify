package com.sparta.sportify.controller.stadiumTime;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumTimeService;
import com.sparta.sportify.util.api.ApiResult;

class StadiumTimeControllerTest {

	@InjectMocks
	private StadiumTimeController stadiumTimeController;

	@Mock
	private StadiumTimeService stadiumTimeService;

	private StadiumTimeRequestDto stadiumTimeRequestDto;
	private StadiumTimeResponseDto stadiumTimeResponseDto;
	private StadiumTime stadiumTime;
	private Stadium stadium;
	private UserDetailsImpl userDetails;
	private User user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		List<String> hours = Arrays.asList("08-12", "14-24");
		List<String> weeks = Arrays.asList("mon", "tue", "thu");

		stadiumTimeRequestDto = new StadiumTimeRequestDto(hours, weeks);

		stadiumTime = StadiumTime.builder()
			.id(1L)
			.cron("0 0 08-12,10-12,20-22 ? * MON,TUE, THU")
			.build();

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
			.user(userDetails.getUser())
			.build();

		stadiumTimeResponseDto = new StadiumTimeResponseDto(stadiumTime, stadium);
	}

	@Test
	@DisplayName("구장 시간 생성 성공")
	void createStadiumTime() {
		Long stadiumId = 1L;
		when(stadiumTimeService.createStadiumTime(eq(stadiumId), any(StadiumTimeRequestDto.class)))
			.thenReturn(stadiumTimeResponseDto);

		ResponseEntity<ApiResult<StadiumTimeResponseDto>> response =
			stadiumTimeController.createStadiumTime(stadiumId, stadiumTimeRequestDto);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장 시간 생성 성공");
		assertThat(response.getBody().getData()).isEqualTo(stadiumTimeResponseDto);

		verify(stadiumTimeService, times(1)).createStadiumTime(eq(stadiumId), any(StadiumTimeRequestDto.class));
	}

	@Test
	@DisplayName("구장 시간 수정 성공")
	void updateStadiumTime() {
		Long stadiumTimeId = 1L;
		when(stadiumTimeService.updateStadiumTime(eq(stadiumTimeId), any(StadiumTimeRequestDto.class),
			any(UserDetailsImpl.class)))
			.thenReturn(stadiumTimeResponseDto);

		ResponseEntity<ApiResult<StadiumTimeResponseDto>> response =
			stadiumTimeController.updateStadiumTime(stadiumTimeId, stadiumTimeRequestDto, userDetails);

		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장 시간 수정 성공");
		assertThat(response.getBody().getData()).isEqualTo(stadiumTimeResponseDto);

		verify(stadiumTimeService, times(1)).updateStadiumTime(eq(stadiumTimeId), any(StadiumTimeRequestDto.class),
			any(UserDetailsImpl.class));

	}
}