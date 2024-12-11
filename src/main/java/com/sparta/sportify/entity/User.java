package com.sparta.sportify.entity;

import com.sparta.sportify.dto.user.req.UserRequestDto;
import jakarta.persistence.*;
import lombok.*;
import com.sparta.sportify.config.PasswordEncoder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String oauthId;
    private String oauthProvider;
    private String region;
    private String gender;
    private Long age;
    private Long levelPoints;

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    private Long cash;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;


    public String getAccessToken() {
        return null;
    }

    public User(UserRequestDto requestDto) {
        this.email = requestDto.getEmail();
        this.password = PasswordEncoder.encode(requestDto.getPassword());
        this.role = requestDto.getRole() != null ? requestDto.getRole() : UserRole.USER;
    }
}
