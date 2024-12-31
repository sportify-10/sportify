package com.sparta.sportify.dto.user.res;

import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignupResponseDto {
    private boolean active;
    private Long levelPoints;
    private String email;
    private String name;
    private UserRole role;  // Enum 타입으로 Role을 포함
    private String region;
    private Long age;
    private String gender;

    public SignupResponseDto(User user, String jwtToken) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();  // UserRole을 Enum으로 설정
        this.region = user.getRegion();  // 지역 정보
        this.age = user.getAge();  // 나이
        this.gender = user.getGender();  // 성별
        this.levelPoints = user.getLevelPoints();  // levelPoints 추가
    }

    public SignupResponseDto(User user) {
        if (user == null) {
            throw new CustomApiException(ErrorCode.USER_NOT_FOUND);
        }
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
        this.region = user.getRegion();
        this.age = user.getAge();
        this.gender = user.getGender();
        this.levelPoints = user.getLevelPoints();
        this.active = user.isActive();
    }



}
