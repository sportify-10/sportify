package com.sparta.sportify.controller;

import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.dto.user.res.LoginResponseDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.security.UserDetailsImpl;  // UserDetailsImpl import
import com.sparta.sportify.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 유저 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signUp(
            @Valid @RequestBody UserRequestDto requestDto,
            @RequestParam(defaultValue = "false") boolean isAdmin // isAdmin을 쿼리 파라미터로 받음
    ) {
        // 회원가입 처리 (isAdmin 값을 이용해 ADMIN 권한 부여)
        User user = userService.signup(requestDto, isAdmin);

        // 응답 데이터 생성
        return ResponseEntity.ok(
                new SignupResponseDto(user)  // 수정된 부분
        );
    }

    // 유저 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        // 로그인 처리
        String token = userService.login(requestDto);

        // 응답 생성
        return ResponseEntity.ok(
                LoginResponseDto.builder()
                        .success(true)
                        .timeStamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))
                        .token(token)
                        .build()
        );
    }

    // 자신의 유저 정보 조회 (자기 자신만 조회 가능)
    @GetMapping("/profile")
    public ResponseEntity<SignupResponseDto> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {

            // userDetailsImpl을 통해 현재 로그인한 사용자의 ID를 가져옴
            Long userId = userDetails.getUser().getId();  // UserDetailsImpl에 저장된 User 정보에서 ID 추출

            SignupResponseDto responseDto = userService.getUserById(userId);

            // 성공적인 경우 200 OK 반환
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

    }

    // 특정 유저 정보 조회 (admin 권한만 가능)
    @GetMapping("/{id}")
    public ResponseEntity<SignupResponseDto> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // admin 권한 확인
        if (userDetails.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
        } else {
            throw new SecurityException("접근 권한이 없습니다.");
        }
    }

}
