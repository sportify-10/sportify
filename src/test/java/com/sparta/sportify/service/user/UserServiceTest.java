package com.sparta.sportify.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.sparta.sportify.config.CustomPasswordEncoder;
import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.dto.user.res.UserDeleteResponseDto;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.repository.UserRepository;
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

import com.sparta.sportify.dto.user.res.UserTeamResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.UserService;


class UserServiceTest {

	@Mock
	TeamMemberRepository teamMemberRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CustomPasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private UserService userService;

	private User user;
	private UserDetailsImpl userDetails;

	private TeamMember teamMember1;
	private TeamMember teamMember2;
	private Team team1;
	private Team team2;


	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

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

		team1 = Team.builder()
			.id(1L)
			.teamName("A team")
			.region("Seoul")
			.skillLevel("Advanced")
			.sportType("Soccer")
			.description("허접 축구팀")
			.teamPoints(1000)
			.winRate(0.75F)
			.deletedAt(null)
			.build();

		team2 = Team.builder()
			.id(2L)
			.teamName("B team")
			.region("Seoul")
			.skillLevel("Advanced")
			.sportType("Soccer")
			.description("허접 축구팀")
			.teamPoints(1000)
			.winRate(0.75F)
			.deletedAt(null)
			.build();

		teamMember1 = TeamMember.builder()
			.teamMemberId(1L)
			.deletedAt(null)
			.status(TeamMember.Status.APPROVED)
			.teamMemberRole(TeamMemberRole.USER)
			.team(team1)
			.user(user)
			.build();

		teamMember2 = TeamMember.builder()
			.teamMemberId(2L)
			.deletedAt(null)
			.status(TeamMember.Status.APPROVED)
			.teamMemberRole(TeamMemberRole.USER)
			.team(team2)
			.user(user)
			.build();
	}

	@Test
	@DisplayName("회원가입 성공")
	void signupSuccess() {
		// Arrange
		UserRequestDto requestDto = new UserRequestDto("test@example.com", "password123", "John Doe", "Seoul", "M", 30);

		when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword123");
		when(userRepository.save(any(User.class))).thenReturn(user);

		// Act
		User result = userService.signup(requestDto, UserRole.USER);

		// Assert
		assertNotNull(result);
		assertEquals(user.getEmail(), result.getEmail());
	}


	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	void signupDuplicateEmail() {
		UserRequestDto requestDto = new UserRequestDto("test@example.com", "password123", "John Doe", "Seoul", "M", 30);

		when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));

		CustomApiException exception = assertThrows(CustomApiException.class, () -> userService.signup(requestDto, UserRole.USER));

		assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() {
		LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "password123");

		when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(true);
		when(jwtUtil.generateToken(user.getEmail(), user.getRole())).thenReturn("jwtToken");

		String token = userService.login(requestDto);

		assertNotNull(token);
		assertTrue(token.startsWith("Bearer "));
	}

	@Test
	@DisplayName("로그인 실패 - 이메일 없음")
	void loginInvalidEmail() {
		LoginRequestDto requestDto = new LoginRequestDto("invalid@example.com", "password123");

		when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());

		CustomApiException exception = assertThrows(CustomApiException.class, () -> userService.login(requestDto));

		assertEquals(ErrorCode.INVALID_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void loginPasswordMismatch() {
		LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "wrongPassword");

		when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword())).thenReturn(false);

		CustomApiException exception = assertThrows(CustomApiException.class, () -> userService.login(requestDto));

		assertEquals(ErrorCode.PASSWORD_MISMATCH, exception.getErrorCode());
	}

	@Test
	@DisplayName("유저 정보 조회 성공")
	void getUserByIdSuccess() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		SignupResponseDto result = userService.getUserById(1L);

		assertNotNull(result);
		assertEquals(user.getEmail(), result.getEmail());
	}

	@Test
	@DisplayName("유저 정보 조회 실패 - 존재하지 않는 ID")
	void getUserByIdNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		CustomApiException exception = assertThrows(CustomApiException.class, () -> userService.getUserById(1L));

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자 비활성화 성공 테스트")
	void deactivateUserSuccess() {
		// 사용자 존재하는 경우
		when(userRepository.findById(44L)).thenReturn(Optional.of(user));

		UserDeleteResponseDto result = userService.deactivateUser(44L);

		// 결과 검증
		assertNotNull(result);
		assertEquals(44L, result.getUserId());
		assertFalse(user.isActive()); // 비활성화 됐는지 확인
	}

	@Test
	@DisplayName("사용자 비활성화 시 사용자 존재하지 않는 경우 예외 테스트")
	void deactivateUserNotFound() {
		// 사용자 존재하지 않는 경우
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			userService.deactivateUser(1L);
		});

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자 정보 수정 성공 테스트")
	void updateUserSuccess() {
		// 준비: requestDto 및 userDetails 초기화
		UserRequestDto requestDto = new UserRequestDto("new@example.com", "New Password", "New Name", "america", "male", 24);

		// user 객체 생성
		User user = User.builder()
				.id(45L)
				.email("testerspring@test.com")
				.password("test1234!")
				.name("이름0")
				.region("지역지역")
				.age(20L)
				.gender("남성")
				.role(UserRole.USER)
				.active(true)
				.levelPoints(1000L)
				.build();

		// UserDetailsImpl 객체 초기화
		UserDetailsImpl userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);

		// userRepository.findById()가 user를 반환하도록 mock 설정
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(userRepository.existsByEmailAndIdNot(requestDto.getEmail(), user.getId())).thenReturn(false);
		when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");

		// userRepository.save()가 저장된 유저를 반환하도록 mock 설정
		when(userRepository.save(any(User.class))).thenReturn(user);  // any(User.class)로 매개변수에 관계없이 반환

		// 실제 업데이트 실행
		SignupResponseDto result = userService.updateUser(requestDto, userDetails);

		// 결과 검증
		assertNotNull(result);
		assertEquals("New Name", result.getName());
		assertEquals("america", result.getRegion()); // 예상대로 지역 업데이트 확인
		assertEquals("encodedPassword", userDetails.getUser().getPassword()); // 비밀번호 암호화 확인
	}


	@Test
	@DisplayName("이메일 중복 검증 실패 시 예외 테스트")
	void updateUserDuplicateEmail() {
		UserRequestDto requestDto = new UserRequestDto("test@example.com", "New Name", "New Region", "Male", "newPassword123");

		when(userRepository.existsByEmailAndIdNot(requestDto.getEmail(), userDetails.getUser().getId())).thenReturn(true);

		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			userService.updateUser(requestDto, userDetails);
		});

		assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("자신이 속한 팀 조회 성공")
	void getUserTeams() {
		Pageable pageable = PageRequest.of(0, 10);

		List<TeamMember> teamMembers = List.of(teamMember1, teamMember2);

		when(teamMemberRepository.findTeams(userDetails.getUser().getId(), pageable))
			.thenReturn(new PageImpl<>(teamMembers));

		Page<UserTeamResponseDto> result = userService.getUserTeams(userDetails, 1, 10);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
	}
}