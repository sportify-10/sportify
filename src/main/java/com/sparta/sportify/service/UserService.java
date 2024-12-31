package com.sparta.sportify.service;

import com.sparta.sportify.config.CustomPasswordEncoder;
import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.dto.user.res.UserDeleteResponseDto;
import com.sparta.sportify.dto.user.res.UserTeamResponseDto;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public User signup(UserRequestDto requestDto, UserRole role) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomApiException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());


        // User 객체 생성
        User user = userRepository.save(User.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .password(encodedPassword)
                .region(requestDto.getRegion())
                .gender(requestDto.getGender())
                .age(Long.valueOf(requestDto.getAge()))
                .role(role)
                .build());

        return user;
    }

    // 로그인
    public String login(LoginRequestDto requestDto) {
        // 이메일로 유저 검색
        User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new CustomApiException(ErrorCode.INVALID_EMAIL)
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomApiException(ErrorCode.PASSWORD_MISMATCH);
        }

        // JWT 토큰 생성 및 반환 (Bearer 형식 포함)
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    // 특정 유저 정보 조회 (ID로 유저 정보 반환)
    @Cacheable(value = "userCache", key = "#userId")
    public SignupResponseDto getUserById(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
        );

        // 유저 정보 DTO로 변환 후 응답 반환
        return new SignupResponseDto(user, null);  // 수정된 부분: SignupResponseDto 생성자로 변환
    }


    @Transactional
    public UserDeleteResponseDto deactivateUser(Long userId) {
        // 요청한 사용자 ID가 존재하는지 확인
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
        );

        // 사용자 비활성화
        user.disableUser();

        userRepository.save(user);

        return new UserDeleteResponseDto(userId);
    }


    public SignupResponseDto updateUser(UserRequestDto requestDto, UserDetailsImpl userDetails) {
        if (requestDto == null) {
            throw new CustomApiException(ErrorCode.INVALID_REQUEST); // requestDto가 null일 경우 예외 처리
        }

        if (userDetails == null) {
            throw new CustomApiException(ErrorCode.USER_NOT_FOUND); // userDetails가 null일 경우 예외 처리
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmailAndIdNot(requestDto.getEmail(), userDetails.getUser().getId())) {
            throw new CustomApiException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = null;

        // 비밀번호 수정이 요청된 경우
        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
            userDetails.getUser().updatePassword(encodedPassword);
        }

        // 수정 가능한 필드만 업데이트
        userDetails.getUser().updateOf(
                requestDto.getName(),
                requestDto.getRegion(),
                requestDto.getGender(),
                requestDto.getAge()
        );

        // 변경된 유저 정보 저장
        return new SignupResponseDto(userRepository.save(userDetails.getUser()));
    }


    // 카카오 로그인 혹은 회원가입 처리
    public SignupResponseDto saveOrLogin(Map<String, Object> userInfo) {
        // 이메일 기반으로 사용자 조회
        String email = (String) userInfo.get("email");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 카카오에서 받은 정보로 회원가입 처리
            UserRequestDto userRequestDto = UserRequestDto.builder()
                    .email(email)
                    .name((String) userInfo.get("name"))
                    .region((String) userInfo.get("region"))
                    .gender((String) userInfo.get("gender"))
                    .age((Long) userInfo.get("age"))
                    .build();

            user = signup(userRequestDto, UserRole.USER);
        }

        // JWT 토큰 생성
        String jwtToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 회원가입 응답 DTO 생성
        return new SignupResponseDto(user, jwtToken); // JWT 토큰 추가
    }

    public Page<UserTeamResponseDto> getUserTeams(UserDetailsImpl userDetails, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        //teamMember 상태 APPROVED 만 조회
        Page<TeamMember> teamMembers = teamMemberRepository.findTeams(userDetails.getUser().getId(), pageable);

        if (teamMembers.isEmpty()) {
            throw new CustomApiException(ErrorCode.TEAM_NOT_REGISTERED);
        }

        return teamMembers.map(teamMember -> new UserTeamResponseDto(
                teamMember.getTeam().getId(),
                teamMember.getTeam().getTeamName(),
                teamMember.getTeam().getRegion(),
                teamMember.getTeam().getActivityTime(),
                teamMember.getTeam().getSkillLevel(),
                teamMember.getTeam().getSportType(),
                teamMember.getTeam().getTeamPoints(),
                Double.parseDouble(String.format("%.2f", teamMember.getTeam().getWinRate())) //0.xx
        ));
    }

    // LevelPoints 순으로 유저 조회
    @Cacheable(value = "usersLevelPoints", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<SignupResponseDto> getAllUsersOrderedByLevelPoints(Pageable pageable) {
        Page<User> userPage = userRepository.findAllByOrderByLevelPointsDesc(pageable);
        if (userPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0); // 사용자 목록이 비어있을 경우 빈 페이지 반환
        }
        List<SignupResponseDto> users = userPage.getContent().stream()
                .map(user -> new SignupResponseDto(user, user.getAccessToken()))
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, userPage.getTotalElements());
    }
}
