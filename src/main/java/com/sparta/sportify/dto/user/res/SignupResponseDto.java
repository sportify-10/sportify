package com.sparta.sportify.dto.user.res;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class SignupResponseDto {
    private String email;
    private String name;
    private UserRole role;  // Enum 타입으로 Role을 포함
    private String region;
    private Long age;
    private String gender;
    private Long levelPoints;  // 새로운 필드 추가
    // private Long userRanking;

    // User와 JWT 토큰으로부터 DTO를 생성하는 생성자
    public SignupResponseDto(User user, String jwtToken) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();  // UserRole을 Enum으로 설정
        this.region = user.getRegion();  // 지역 정보
        this.age = user.getAge();  // 나이
        this.gender = user.getGender();  // 성별
        this.levelPoints = user.getLevelPoints();  // levelPoints 추가
        // this.userRanking = user.getUserRanking();
    }
    // 테스트용 생성자
    public SignupResponseDto(long l, String testUsername, String mail) {
        this.email = mail;
        this.name = testUsername;
        this.levelPoints = l;  // levelPoints 추가
        // this.userRanking = r;
    }
}