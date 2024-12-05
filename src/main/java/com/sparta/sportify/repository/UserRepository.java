package com.sparta.sportify.repository;

import com.sparta.sportify.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(@NotBlank(message = "이메일은 필수 항목입니다.") @Email(message = "올바른 이메일 형식이어야 합니다.") String email);
}
