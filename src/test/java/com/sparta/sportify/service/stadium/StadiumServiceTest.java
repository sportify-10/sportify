package com.sparta.sportify.service.stadium;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumMatchResponseDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.ReservationStatus;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.StadiumRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumService;

class StadiumServiceTest {

	@Mock
	private StadiumRepository stadiumRepository;

	@InjectMocks
	private StadiumService stadiumService;

	@Mock
	private MatchRepository matchRepository;

	private StadiumCreateRequestDto stadiumCreateRequestDto;
	private StadiumUpdateRequestDto stadiumUpdateRequestDto;

	private UserDetailsImpl userDetails;
	private UserDetailsImpl notUserDetails;
	private User user;
	private User notOwner;
	private Stadium stadium;
	private Match match;
	private StadiumTime stadiumTime;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		stadiumCreateRequestDto = new StadiumCreateRequestDto("A구장", "서울", 6, 6, "넓고 좋은 경기장", 100000L);
		stadiumUpdateRequestDto = new StadiumUpdateRequestDto("B구장", "서울", 6, 6, "넓고 좋은 경기장", 100000L);

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
			.user(userDetails.getUser())
			.build();
		when(stadiumRepository.save(any(Stadium.class))).thenReturn(stadium);

		stadiumTime = StadiumTime.builder()
			.id(1L)
			.cron("0 0 08-10,10-12,20-22 ? * MON,TUE")
			.stadium(stadium)
			.build();
		when(stadiumRepository.findById(stadium.getId())).thenReturn(Optional.of(stadium));

		match = Match.builder()
			.id(1L)
			.date(LocalDate.now())
			.time(20)
			.aTeamCount(5)
			.bTeamCount(5)
			.stadiumTime(stadiumTime)
			.build();
		when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

	}

	@Test
	@DisplayName("구장 생성 성공")
	void createStadium() {
		assertNotNull(stadiumService.createStadium(stadiumCreateRequestDto, userDetails));
		verify(stadiumRepository, times(1)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 이름 중복되어 에러")
	void existStadiumName() {
		when(stadiumRepository.findByStadiumName(stadiumCreateRequestDto.getStadiumName()))
			.thenReturn(Optional.of(new Stadium()));

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.createStadium(stadiumCreateRequestDto, userDetails);
		});

		assertEquals("구장 이름이 이미 존재합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 정보 수정")
	void updateStadium() {
		stadiumService.updateStadium(stadium.getId(), stadiumUpdateRequestDto, userDetails);

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
		when(stadiumRepository.findById(stadium.getId())).thenReturn(Optional.empty());
		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.updateStadium(stadium.getId(), stadiumUpdateRequestDto, userDetails);
		});

		assertEquals("구장이 존재하지 않습니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 주인이 아닌 사람의 수정 요청 시 에러")
	void notOwnerStadium() {
		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.updateStadium(stadium.getId(), stadiumUpdateRequestDto, notUserDetails);
		});

		assertEquals("자신의 구장만 수정 가능합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("구장 삭제")
	void deleteStadium() {
		stadiumService.deleteStadium(stadium.getId(), userDetails);

		assertNotNull(stadium.getDeletedAt());
	}

	@Test
	@DisplayName("구장 주인이 아닌 사람의 삭제 요청 시 에러")
	void notOwnerDeleteStadium() {
		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.deleteStadium(stadium.getId(), notUserDetails);
		});

		assertEquals("자신의 구장만 삭제 가능합니다", thrown.getMessage());
		verify(stadiumRepository, times(0)).save(any(Stadium.class));
	}

	@Test
	@DisplayName("자신의 구장 조회")
	void getStadiumsMine() {
		Stadium stadium1 = Stadium.builder()
			.id(1L)
			.stadiumName("Stadium A")
			.location("Location A")
			.user(user)
			.build();

		Stadium stadium2 = Stadium.builder()
			.id(2L)
			.stadiumName("Stadium B")
			.location("Location B")
			.user(user)
			.build();

		Page<Stadium> stadiumPage = new PageImpl<>(Arrays.asList(stadium1, stadium2));

		when(stadiumRepository.findAllByUserId(any(Long.class), any(Pageable.class)))
			.thenReturn(stadiumPage);

		Page<StadiumResponseDto> response = stadiumService.getStadiums(userDetails, 1, 10);

		assertNotNull(response);
		assertEquals(2, response.getContent().size());
	}

	@Test
	@DisplayName("자신의 구장이 없을 때 조회 시 예외")
	void getStadiumsNull() {
		Page<Stadium> stadiumPage = Page.empty();
		when(stadiumRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(stadiumPage);

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.getStadiums(userDetails, 1, 10);
		});

		assertEquals("등록한 구장이 없습니다", thrown.getMessage());
		verify(stadiumRepository, times(1)).findAllByUserId(user.getId(), PageRequest.of(0, 10));
	}

	@Test
	@DisplayName("구장에 예약된 매치 조회")
	void findMatchesByStadium() {
		List<Object[]> content = List.of(
			new Object[] {match, 10000L},
			new Object[] {match, null}
		);

		Pageable pageable = PageRequest.of(0, 10);
		Page<Object[]> pageResult = new PageImpl<>(content, pageable, content.size());

		when(matchRepository.findMatchesWithTotalAmountByStadiumId(eq(1L), eq(ReservationStatus.CONFIRMED),
			any(Pageable.class)))
			.thenReturn(pageResult);

		Page<StadiumMatchResponseDto> result = stadiumService.findMatchesByStadium(1L, 1, 10);

		assertEquals(2, result.getTotalElements());

		StadiumMatchResponseDto responseDto = result.getContent().get(0);
		assertEquals("Dream Stadium", responseDto.getStadiumName());
		assertEquals(10000, responseDto.getTotalAmount());
		assertEquals(5, responseDto.getTeamACount());
		assertEquals(5, responseDto.getTeamBCount());

		verify(stadiumRepository, times(1)).findById(1L);
		verify(matchRepository, times(1)).findMatchesWithTotalAmountByStadiumId(eq(1L), eq(ReservationStatus.CONFIRMED),
			any(Pageable.class));
	}

	@Test
	@DisplayName("저장된 구장이 없어 에러메시지")
	void findMatchesByStadium_noStadium() {
		Long stadiumId = 999L;
		int page = 1;
		int size = 10;

		when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());

		CustomApiException thrown = assertThrows(CustomApiException.class, () -> {
			stadiumService.findMatchesByStadium(stadiumId, page, size);
		});

		assertEquals("구장이 존재하지 않습니다", thrown.getMessage());
	}
}