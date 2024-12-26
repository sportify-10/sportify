//package com.sparta.sportify.service.user;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sparta.sportify.controller.user.UserController;
//import com.sparta.sportify.dto.user.req.LoginRequestDto;
//import com.sparta.sportify.dto.user.req.UserRequestDto;
//import com.sparta.sportify.dto.user.res.LoginResponseDto;
//import com.sparta.sportify.dto.user.res.SignupResponseDto;
//import com.sparta.sportify.entity.user.User;
//import com.sparta.sportify.entity.user.UserRole;
//import com.sparta.sportify.jwt.JwtUtil;
//import com.sparta.sportify.service.UserService;
//import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
//import com.sparta.sportify.util.api.ApiResult;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)
//public class UserControllerTest {
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private CustomOAuth2UserService customOAuth2UserService;
//
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @InjectMocks
//    private UserController userController;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    public void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
//    }
//    @Test
//    public void testSignUpWithValidUserRequestDto() {
//        UserRequestDto userRequestDto = new UserRequestDto();
//        userRequestDto.setPassword("validPassword");
//        userRequestDto.setEmail("validEmail@example.com");
//        userRequestDto.setRegion("validRegion");
//        userRequestDto.setName("validName");
//        userRequestDto.setGender("male");
//        userRequestDto.setAge(25L);
//
//        ResponseEntity<ApiResult<User>> response = userController.signUp(userRequestDto);
//
//        // 유효성 검사 통과한 경우 응답 코드 및 메시지 검증
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        // 기타 필요 검사들
//    }
//
//
//    @Test
//    public void testLogin() throws Exception {
//        // Given
//        LoginRequestDto requestDto = new LoginRequestDto("validEmail@example.com", "validPassword");
//        String expectedToken = "token";  // 예상 토큰 값
//        when(userService.login(requestDto)).thenReturn(expectedToken);
//
//        // When & Then
//        mockMvc.perform(post("/api/users/login")
//                        .contentType("application/json")
//                        .content(new ObjectMapper().writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("로그인 성공"))
//                .andExpect(jsonPath("$.data.token").value(expectedToken));
//    }
//
//    @Test
//    public void testGetUserProfile() throws Exception {
//        // Given
//        SignupResponseDto responseDto = new SignupResponseDto();
//        when(userService.getUserById(1L)).thenReturn(responseDto);
//
//        // When & Then
//        mockMvc.perform(get("/api/users/profile")
//                        .header("Authorization", "Bearer mockToken"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("프로필 조회 성공"))
//                .andExpect(jsonPath("$.data.username").value("username"));
//    }
//
//    @Test
//    public void testDeleteUser() throws Exception {
//        // When
//        mockMvc.perform(delete("/api/users/profile")
//                        .header("Authorization", "Bearer mockToken"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("계정이 비활성화되었습니다."));
//    }
//
//    @Test
//    public void testDeleteUserByAdmin() throws Exception {
//        // Given
//        Long userId = 1L;
//        doReturn(true).when(userService).deactivateUser(userId);
//
//
//        // When & Then
//        mockMvc.perform(delete("/api/users/" + userId)
//                        .header("Authorization", "Bearer adminToken")
//                        .header("Role", "ADMIN"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("사용자 계정이 비활성화되었습니다."));
//    }
//
//    @Test
//    public void testKakaoLogin() throws Exception {
//        // Given
//        OAuth2User oAuth2User = mock(OAuth2User.class);
//        when(customOAuth2UserService.extractUserAttributes(oAuth2User)).thenReturn("email@example.com");
//
//        // When & Then
//        mockMvc.perform(get("/api/users/kakao/login")
//                        .header("Authorization", "Bearer mockToken"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.message").value("카카오 로그인 성공"))
//                .andExpect(jsonPath("$.data").value("email@example.com"));
//    }
//}
//
