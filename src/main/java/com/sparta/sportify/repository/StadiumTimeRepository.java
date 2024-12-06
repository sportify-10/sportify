package com.sparta.sportify.repository;

import java.util.Optional;

import com.sparta.sportify.entity.StadiumTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadiumTimeRepository extends JpaRepository<StadiumTime, Long> {
	Optional<StadiumTime> findByStadiumId(Long stadiumId);
}
