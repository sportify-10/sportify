package com.sparta.sportify.service;

import com.sparta.sportify.config.PasswordEncoder;
import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public SignupResponseDto signup(UserRequestDto requestDto, UserRole role) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("중복된 이메일이 존재합니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());


        // User 객체 생성
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setName(requestDto.getName());
        user.setPassword(encodedPassword);
        user.setRegion(requestDto.getRegion());
        user.setGender(requestDto.getGender());
        user.setAge(Long.valueOf(requestDto.getAge()));
        user.setRole(role); // 수정된 부분: role을 설정하도록 수정

        // 저장
        userRepository.save(user);

        return new SignupResponseDto(user);
    }

    // 로그인
    public String login(LoginRequestDto requestDto) {
        // 이메일로 유저 검색
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성 및 반환 (Bearer 형식 포함)
        return "Bearer " + jwtUtil.generateToken(user.getEmail(), user.getRole());
    }

    // 특정 유저 정보 조회 (ID로 유저 정보 반환)
    public SignupResponseDto getUserById(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 유저 정보 DTO로 변환 후 응답 반환
        return new SignupResponseDto(user);  // 수정된 부분: SignupResponseDto 생성자로 변환
    }

    @Transactional
    public void deactivateUser(Long userId) {
        // 요청한 사용자 ID가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 사용자 비활성화
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }


    // 유저 정보 수정
    public void updateUser(UserRequestDto requestDto, UserDetailsImpl userDetails) {
        // 이메일 중복 검사
        if (userRepository.existsByEmailAndIdNot(requestDto.getEmail(), userDetails.getUser().getId())) {
            throw new RuntimeException("이메일이 중복되었습니다.");
        }

        // 비밀번호 수정이 요청된 경우
        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            // 비밀번호를 암호화해서 저장
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
            userDetails.getUser().setPassword(encodedPassword);
        }

        // 수정 가능한 필드만 업데이트
        userDetails.getUser().setName(requestDto.getName());
        userDetails.getUser().setRegion(requestDto.getRegion());
        userDetails.getUser().setAge(requestDto.getAge());
        userDetails.getUser().setGender(requestDto.getGender());

        // 변경된 유저 정보 저장
        userRepository.save(userDetails.getUser());
    }
}
