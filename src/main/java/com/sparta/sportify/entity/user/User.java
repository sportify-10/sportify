package com.sparta.sportify.entity.user;

import com.sparta.sportify.config.PasswordEncoder;
import com.sparta.sportify.dto.user.req.UserRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
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

    @Column(unique = true)
    private String oauthId;

    private String oauthProvider;
    private String region;
    private String gender;
    private Long age;
    @Builder.Default
    private Long levelPoints=1000L;

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
    @Builder.Default
    private boolean active = true;

    public void addCash(Long amount) {
        if (this.cash == null) {
            this.cash = 0L;
        }
        this.cash += amount;
    }

    public void subCash(Long amount) {
        if (this.cash == null) {
            this.cash = 0L;
        }
        this.cash -= amount;
    }

    public String getAccessToken() {
        return null;
    }

    public void disableUser() {
        this.active = false;
        this.deletedAt = LocalDateTime.now();
    }

    public User(UserRequestDto requestDto) {
        this.email = requestDto.getEmail();
        this.password = PasswordEncoder.encode(requestDto.getPassword());
        this.role = requestDto.getRole() != null ? requestDto.getRole() : UserRole.USER;
    }
}
