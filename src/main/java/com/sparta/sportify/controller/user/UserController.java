package com.sparta.sportify.controller.user;

import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.LoginResponseDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.dto.user.res.UserDeleteResponseDto;
import com.sparta.sportify.dto.user.res.UserTeamResponseDto;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.UserService;
import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
import com.sparta.sportify.util.api.ApiResult;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final CustomOAuth2UserService customOAuth2UserService;

    // 유저 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResult<SignupResponseDto>> signUp(
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        UserRole role = (requestDto.getRole() != null) ? requestDto.getRole() : UserRole.USER;

        // 응답 데이터 생성
        return new ResponseEntity<>(
                ApiResult.success(
                        "회원가입 성공",
                        new SignupResponseDto(userService.signup(requestDto, role))
                ),
                HttpStatus.OK
        );
    }


    // 유저 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "로그인 성공",
                        new LoginResponseDto(userService.login(requestDto))
                ),
                HttpStatus.OK
        );
    }

    // 자신의 유저 정보 조회 (자기 자신만 조회 가능)
    @GetMapping("/profile")
    public ResponseEntity<ApiResult<SignupResponseDto>> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(
                ApiResult.success("프로필 조회 성공", userService.getUserById(userDetails.getUser().getId())),
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
                ApiResult.success(
                        "유저 정보 조회 성공",
                        responseDto
                ),
                HttpStatus.OK
        );
    }


    // 자신의 계정 삭제
    @DeleteMapping("/profile")
    public ResponseEntity<ApiResult<UserDeleteResponseDto>> deleteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<>(
                ApiResult.success("계정이 비활성화되었습니다.", userService.deactivateUser(userDetails.getUser().getId())),
                HttpStatus.OK
        );
    }

    // 관리자가 특정 유저 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResult<UserDeleteResponseDto>> deleteUserByAdmin(
            @PathVariable Long id
    ) {
        return new ResponseEntity<>(
                ApiResult.success("사용자 계정이 비활성화되었습니다.", userService.deactivateUser(id)),
                HttpStatus.OK
        );
    }


    // 유저 정보 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResult<SignupResponseDto>> updateUser(
            @Valid @RequestBody UserRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return new ResponseEntity<>(
                ApiResult.success("유저 정보가 수정되었습니다.",
                        userService.updateUser(requestDto, userDetails)
                ),
                HttpStatus.OK
        );
    }


    @GetMapping("/oAuth/login")
    public ResponseEntity<ApiResult<String>> oAuthLogin(@AuthenticationPrincipal OAuth2User oAuth2User, @RequestParam("provider") String provider) {
        if (oAuth2User == null) {
            throw new CustomApiException(ErrorCode.MISSING_OAUTH2_USER_INFO);
        }

        // provider에 따라 이메일 추출 메서드를 호출
        String email = null;
        switch (provider.toLowerCase()) {
            case "kakao":
                email = customOAuth2UserService.extractUserAttributes(oAuth2User);
                break;
            case "naver":
                email = customOAuth2UserService.extractNaverUserAttributes(oAuth2User);
                break;
            case "google":
                email = customOAuth2UserService.extractGoogleUserAttributes(oAuth2User);
                break;
            default:
                throw new CustomApiException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        String responseMessage = (email == null) ? "Email not available" : email;

        return ResponseEntity.ok(
                ApiResult.success(
                        provider + " 로그인 성공",
                        responseMessage
                )
        );
    }


    @GetMapping("/kakao/login")
    public RedirectView redirectToKakao() {
        return new RedirectView("/oauth2/authorization/kakao");
    }

    @GetMapping("/naver/login")
    public RedirectView redirectToNaver() {
        return new RedirectView("/oauth2/authorization/naver");
    }

    @GetMapping("/google/login")
    public RedirectView redirectToGoogle() {
        return new RedirectView("/oauth2/authorization/google");
    }

    //유저의 팀 조회
    @GetMapping("/team")
    public ResponseEntity<ApiResult<Page<UserTeamResponseDto>>> getUserTeams(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "유저의 팀 조회 성공",
                        userService.getUserTeams(userDetails, page, size)
                )
        );
    }

    // LevelPoints 순으로 유저 조회
    @GetMapping("/levelPoints")
    public ResponseEntity<ApiResult<Page<SignupResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);

        return new ResponseEntity<>(
                ApiResult.success(
                        "전체 사용자 조회 성공",
                        userService.getAllUsersOrderedByLevelPoints(pageable)
                ),
                HttpStatus.OK
        );
    }
}



