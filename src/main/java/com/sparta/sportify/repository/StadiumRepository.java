package com.sparta.sportify.repository;

import java.util.List;
import java.util.Optional;

import com.sparta.sportify.entity.Stadium;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
	Optional<Stadium> findByStadiumName(String StadiumName);

	Page<Stadium> findAllByUserId(Long id, Pageable pageable);
}
