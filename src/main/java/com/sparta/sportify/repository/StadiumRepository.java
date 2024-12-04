package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
}
