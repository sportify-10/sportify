package com.sparta.sportify.repository;

import com.sparta.sportify.entity.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<User> findByEmail(@NotBlank(message = "이메일은 필수 항목입니다.") @Email(message = "올바른 이메일 형식이어야 합니다.") String email);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);


    Optional<User> findByOauthId(String oauthId);

    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findUsersByIdIn(@Param("userIds") List<Long> userIds);

    @Query("SELECT u FROM User u ORDER BY u.levelPoints DESC")
    Page<User> findAllByOrderByLevelPointsDesc(Pageable pageable);
}
