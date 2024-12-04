package com.sparta.sportify.repository;

import com.sparta.sportify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
