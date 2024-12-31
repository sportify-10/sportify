package com.sparta.sportify.dto.user.req;

import com.sparta.sportify.entity.user.UserRole;
import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;
    private String oauthId; // OAuth2 사용자 고유 ID
    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
            message = "비밀번호는 최소 8자 이상이어야 하며, 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "지역은 필수 항목입니다.")
    private String region;

    @NotBlank(message = "성별은 필수 항목입니다.")
    @Pattern(regexp = "^(male|female|other)$", message = "성별은 'male', 'female', 또는 'other' 중 하나여야 합니다.")
    private String gender;

    @NotNull(message = "나이는 필수 항목입니다.")
    @Min(value = 1, message = "나이는 1살 이상이어야 합니다.")
    private Long age;

    private UserRole role;


    public UserRequestDto(String email, String password, String name, String region, String gender, int age) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.region = region;
        this.gender = gender;
        this.age = (long) age; // 테스트 코드와 필드 타입이 일치하지 않는 부분 해결
    }

    public UserRequestDto(String email, String name, String region, String gender, String password) {
        this.email = email;
        this.name = name;
        this.region = region;
        this.gender = gender;
        this.password = password;
    }
}
