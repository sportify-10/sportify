package com.sparta.sportify.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.user.req.LoginRequestDto;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import com.sparta.sportify.dto.user.res.SignupResponseDto;
import com.sparta.sportify.dto.user.res.UserDeleteResponseDto;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.jwt.JwtUtil;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.UserService;
import com.sparta.sportify.service.oauth.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserDetailsImpl authUser;

    @InjectMocks
    private UserController userController;

    private User user;
    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("testerspring1@test.com")
                .age(30L)
                .cash(30000L)
                .name("Test User")
                .gender("male")
                .levelPoints(1000L)
                .password("Password123!")
                .role(UserRole.USER)
                .region("Seoul")
                .build();
    }

    @Test
    void signUp_success() throws Exception {
        // Given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .email("testerspring1@test.com")
                .oauthId("oauth12345")
                .name("Test User")
                .password("Password123!")
                .region("Seoul")
                .gender("male")
                .age(30L)
                .role(UserRole.USER)
                .build();

        SignupResponseDto signupResponseDto = new SignupResponseDto(user);

        when(userService.signup(any(), any())).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    void login_success() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto("testerspring1@test.com", "Password123!");

        when(userService.login(any())).thenReturn("token");

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    @WithMockCustomUser
    void getUserProfile_success() throws Exception {
        // Given
        SignupResponseDto signupResponseDto = new SignupResponseDto(user);

        when(userService.getUserById(any())).thenReturn(signupResponseDto);

        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 조회 성공"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }


    @Test
    @WithMockCustomUser
    void getUserById_success() throws Exception {
        // Given
        Long userId = 1L;
        SignupResponseDto signupResponseDto = new SignupResponseDto(user);


        when(userService.getUserById(userId)).thenReturn(signupResponseDto);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 정보 조회 성공"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    @WithMockCustomUser
    void deleteUser_success() throws Exception {
        // Given
        when(userService.deactivateUser(any())).thenReturn(new UserDeleteResponseDto(1L));
        when(authUser.getUser()).thenReturn(user);


        // When & Then
        mockMvc.perform(delete("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("계정이 비활성화되었습니다."));
    }

    @Test
    @WithMockCustomUser
    void deleteUserByAdmin_success() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.deactivateUser(userId)).thenReturn(new UserDeleteResponseDto(1L));

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 계정이 비활성화되었습니다."));
    }

    @Test
    @WithMockCustomUser
    void updateUser_success() throws Exception {
        // Given
        UserRequestDto userRequestDto = UserRequestDto.builder()
                .email("testerspring1@test.com")
                .oauthId("oauth12345")
                .name("Test User")
                .password("Password123!")
                .region("Seoul")
                .gender("male")
                .age(30L)
                .role(UserRole.USER)
                .build();

        SignupResponseDto signupResponseDto = new SignupResponseDto(user);

        when(userService.updateUser(any(), any())).thenReturn(signupResponseDto);

        // When & Then
        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    @WithMockCustomUser
    void getUserTeams_success() throws Exception {
        // Given
        when(userService.getUserTeams(any(), eq(0), eq(5))).thenReturn(null); // Replace with actual mock data

        // When & Then
        mockMvc.perform(get("/api/users/team")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저의 팀 조회 성공"));
    }

    @Test
    @WithMockCustomUser
    void getAllUsers_success() throws Exception {
        // Given
        when(userService.getAllUsersOrderedByLevelPoints(any())).thenReturn(null); // Replace with actual mock data

        // When & Then
        mockMvc.perform(get("/api/users/levelPoints")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("전체 사용자 조회 성공"));
    }
}
