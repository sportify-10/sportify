package com.sparta.sportify.service.cash;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.entity.CashLog;
import com.sparta.sportify.entity.CashType;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CashService;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {


	@InjectMocks
	private CashService cashService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CashLogRepository cashLogRepository;

	private User user;
	private UserDetailsImpl userDetails;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.id(1L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L) // 선택적 필드
			.build();

		userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);
	}

	@Test
	@DisplayName("캐시 추가 성공")
	void addCash() {
		CashRequestDto cashRequestDto = new CashRequestDto(1000L);
		CashLog cashLog = CashLog.builder()
			.id(1L)
			.price(cashRequestDto.getAmount())
			.createAt(LocalDateTime.now())
			.type(CashType.CHARGE)
			.user(userDetails.getUser())
			.build();

		when(cashLogRepository.save(any(CashLog.class))).thenReturn(cashLog);

		cashService.addCash(userDetails, cashRequestDto);

		verify(cashLogRepository, times(1)).save(any(CashLog.class));
		verify(userRepository, times(1)).save(user);
		assertEquals(2000L, user.getCash());
	}

	@Test
	@DisplayName("캐시 로그가 없을 때 예외 발생")
	void getCashLogsEmpty() {
		Page<CashLog> emptyPage = Page.empty();
		when(cashLogRepository.findAllByUserId(any(Long.class), any(Pageable.class)))
			.thenReturn(emptyPage);

		assertThrows(IllegalArgumentException.class, () -> {
			cashService.getCashLogs(userDetails, 1, 10);
		});
	}

	@Test
	@DisplayName("캐시 조회 성공")
	void getCashLogs() {
		CashLog cashLog1 = CashLog.builder()
			.id(1L)
			.price(500L)
			.createAt(LocalDateTime.now())
			.type(CashType.CHARGE)
			.user(user)
			.build();

		CashLog cashLog2 = CashLog.builder()
			.id(2L)
			.price(300L)
			.createAt(LocalDateTime.now())
			.type(CashType.CHARGE)
			.user(user)
			.build();

		Page<CashLog> cashLogPage = new PageImpl<>(Arrays.asList(cashLog1, cashLog2));

		when(cashLogRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(cashLogPage);

		Page<CashLogsResponseDto> response = cashService.getCashLogs(userDetails, 1, 10);

		assertNotNull(response);
		assertEquals(2, response.getContent().size());

		CashLogsResponseDto responseDto1 = response.getContent().get(0);
		assertEquals(500L, responseDto1.getAmount());
		assertEquals(cashLog1.getCreateAt(), responseDto1.getCreateAt());
		assertEquals(cashLog1.getType(), responseDto1.getType());

		CashLogsResponseDto responseDto2 = response.getContent().get(1);
		assertEquals(300L, responseDto2.getAmount());
		assertEquals(cashLog2.getCreateAt(), responseDto2.getCreateAt());
		assertEquals(cashLog2.getType(), responseDto2.getType());
	}
}