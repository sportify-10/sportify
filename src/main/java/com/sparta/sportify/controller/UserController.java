package com.sparta.sportify.controller;

import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.LoginResponseDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.entity.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.UserService;
import com.sparta.sportify.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 유저 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResult<SignupResponseDto>> signUp(
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        // 역할이 없는 경우 기본값 USER 설정
        UserRole role = (requestDto.getRole() != null) ? requestDto.getRole() : UserRole.USER;

        // 응답 데이터 생성
        return new ResponseEntity<>(
                ApiResult.success("회원가입 성공",
                        userService.signup(requestDto, role)),
                HttpStatus.OK
        );
    }


    // 유저 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) {
        // 로그인 처리
        String token = userService.login(requestDto);

        // 응답 생성
        LoginResponseDto responseDto = LoginResponseDto.builder()
                .success(true)
                .timeStamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))
                .token(token)
                .build();

        return new ResponseEntity<>(
                ApiResult.success("로그인 성공", responseDto),
                HttpStatus.OK
        );
    }

    // 자신의 유저 정보 조회 (자기 자신만 조회 가능)
    @GetMapping("/profile")
    public ResponseEntity<ApiResult<SignupResponseDto>> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 현재 로그인한 사용자의 ID로 정보 조회
        SignupResponseDto responseDto = userService.getUserById(userDetails.getUser().getId());

        return new ResponseEntity<>(
                ApiResult.success("프로필 조회 성공", responseDto),
                HttpStatus.OK
        );
    }

    // 특정 유저 정보 조회 (admin 권한만 가능)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<SignupResponseDto>> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
            SignupResponseDto responseDto = userService.getUserById(id);
                return new ResponseEntity<>(
                        ApiResult.success("유저 정보 조회 성공", responseDto),
                        HttpStatus.OK
                );
        }


    // 자신의 계정 삭제
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResult<String>> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.deactivateUser(userDetails.getUser().getId());
        return new ResponseEntity<>(
                ApiResult.success("계정이 비활성화되었습니다.", null),
                HttpStatus.OK
        );
    }

    // 관리자가 특정 유저 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<String>> deleteUserByAdmin(
            @PathVariable Long id // 삭제 대상 유저의 ID
    ) {
        // 전달받은 id를 사용해 해당 유저를 비활성화
        userService.deactivateUser(id);

        return new ResponseEntity<>(
                ApiResult.success("사용자 계정이 비활성화되었습니다.", null),
                HttpStatus.OK
        );
    }

}

