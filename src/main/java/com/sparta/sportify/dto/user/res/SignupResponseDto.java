package com.sparta.sportify.dto.user.res;

import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupResponseDto {
    private String email;
    private String name;
    private UserRole role;  // Enum 타입으로 Role을 포함
    private String region;
    private Long age;
    private String gender;

    public SignupResponseDto(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();  // UserRole을 Enum으로 설정
        this.region = user.getRegion();  // 지역 정보
        this.age = user.getAge();  // 나이
        this.gender = user.getGender();  // 성별
    }

    // 성공적인 응답을 반환할 수 있는 메서드
    public static SignupResponseDto of(User user) {
        return new SignupResponseDto(user);
    }
}
