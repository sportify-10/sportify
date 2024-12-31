package com.sparta.sportify.controller.stadium;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumMatchResponseDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumService;
import com.sparta.sportify.util.api.ApiResult;

class StadiumControllerTest {

	@InjectMocks
	private StadiumController stadiumController;

	@Mock
	private StadiumService stadiumService;

	private UserDetailsImpl userDetails;
	private User user;
	private StadiumCreateRequestDto stadiumCreateRequestDto;
	private StadiumResponseDto stadiumResponseDto;
	private StadiumResponseDto stadiumResponseDto2;
	private StadiumMatchResponseDto match1;
	private StadiumMatchResponseDto match2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000L);

		stadiumResponseDto = StadiumResponseDto.builder()
			.id(1L)
			.stadiumName("A구장")
			.location("서울")
			.aTeamCount(6)
			.bTeamCount(6)
			.description("넓고 좋은 경기장")
			.price(100000L)
			.status(StadiumStatus.APPROVED)
			.deletedAt(null)
			.build();

		stadiumResponseDto2 = StadiumResponseDto.builder()
			.id(1L)
			.stadiumName("B구장")
			.location("부산")
			.aTeamCount(6)
			.bTeamCount(6)
			.description("넓고 좋은 경기장")
			.price(100000L)
			.status(StadiumStatus.APPROVED)
			.deletedAt(null)
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

		match1 = StadiumMatchResponseDto.builder()
			.stadiumId(1L)
			.stadiumName("A구장")
			.matchDate(LocalDate.of(2024, 12, 30))
			.matchTime("14:00")
			.totalAmount(50000)
			.teamACount(4)
			.teamBCount(6)
			.build();

		match2 = StadiumMatchResponseDto.builder()
			.stadiumId(2L)
			.stadiumName("B구장")
			.matchDate(LocalDate.of(2024, 12, 30))
			.matchTime("14:00")
			.totalAmount(50000)
			.teamACount(4)
			.teamBCount(6)
			.build();
	}

	@Test
	@DisplayName("구장 생성 성공")
	void createStadium() {
		when(stadiumService.createStadium(any(StadiumCreateRequestDto.class), any(UserDetailsImpl.class)))
			.thenReturn(stadiumResponseDto);

		ResponseEntity<ApiResult<StadiumResponseDto>> response = stadiumController.createStadium(
			stadiumCreateRequestDto, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(stadiumService, times(1)).createStadium(any(StadiumCreateRequestDto.class), any(UserDetailsImpl.class));
	}

	@Test
	@DisplayName("구장 조회 성공")
	void getStadiums() {
		Page<StadiumResponseDto> stadiumPage = new PageImpl<>(List.of(stadiumResponseDto));

		// StadiumService 모킹
		Mockito.when(stadiumService.getStadiums(eq(userDetails), eq(1), eq(5)))
			.thenReturn(stadiumPage);

		// 컨트롤러 호출
		ResponseEntity<ApiResult<Page<StadiumResponseDto>>> response = stadiumController.getStadiums(userDetails, 1, 5);

		// 검증
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장 조회 성공");
		assertThat(response.getBody().getData().getContent().get(0).getStadiumName()).isEqualTo("A구장");
	}

	@Test
	@DisplayName("구장 수정 성공")
	void updateStadium() {
		StadiumUpdateRequestDto updateRequest = new StadiumUpdateRequestDto("수정된 구장", "부산", 8, 8, "수정된 설명", 120000L);
		StadiumResponseDto updatedStadiumResponse = StadiumResponseDto.builder()
			.id(1L)
			.stadiumName(updateRequest.getStadiumName())
			.location(updateRequest.getLocation())
			.aTeamCount(updateRequest.getATeamCount())
			.bTeamCount(updateRequest.getBTeamCount())
			.description(updateRequest.getDescription())
			.price(updateRequest.getPrice())
			.status(StadiumStatus.APPROVED)
			.deletedAt(null)
			.build();

		// StadiumService 모킹
		Mockito.when(stadiumService.updateStadium(eq(1L), eq(updateRequest), eq(userDetails)))
			.thenReturn(updatedStadiumResponse);

		// 컨트롤러 호출
		ResponseEntity<ApiResult<StadiumResponseDto>> response = stadiumController.updateStadium(1L, updateRequest,
			userDetails);

		// 검증
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장 수정 성공");
		assertThat(response.getBody().getData().getStadiumName()).isEqualTo("수정된 구장");
	}

	@Test
	@DisplayName("구장 삭제 성공")
	void deleteStadium() {
		Mockito.when(stadiumService.deleteStadium(eq(1L), eq(userDetails)))
			.thenReturn(stadiumResponseDto);

		// 컨트롤러 호출
		ResponseEntity<ApiResult<StadiumResponseDto>> response = stadiumController.deleteStadium(1L, userDetails);

		// 검증
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장 삭제 성공");
		assertThat(response.getBody().getData().getStadiumName()).isEqualTo("A구장");
	}

	@Test
	@DisplayName("구장에 예약된 매치 조회 성공")
	void findMatchesByStadium() {
		Page<StadiumMatchResponseDto> matchPage = new PageImpl<>(List.of(match1, match2));

		// StadiumService 모킹
		Mockito.when(stadiumService.findMatchesByStadium(eq(1L), eq(1), eq(5)))
			.thenReturn(matchPage);

		// 컨트롤러 호출
		ResponseEntity<ApiResult<Page<StadiumMatchResponseDto>>> response = stadiumController.findMatchesByStadium(1L,
			1, 5);

		// 검증
		assertThat(response.getStatusCodeValue()).isEqualTo(200);
		assertThat(response.getBody().getMessage()).isEqualTo("구장에 예약된 매치 조회 성공");
		assertThat(response.getBody().getData().getContent().size()).isEqualTo(2);
		assertThat(response.getBody().getData().getContent().get(0).getStadiumName()).isEqualTo("A구장");
		assertThat(response.getBody().getData().getContent().get(1).getStadiumName()).isEqualTo("B구장");
	}
}