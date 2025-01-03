package com.sparta.sportify.repository;

import java.util.List;
import java.util.Optional;

import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StadiumTimeRepository extends JpaRepository<StadiumTime, Long> {
	Optional<StadiumTime> findByStadiumId(Long stadiumId);
	//FULLTEXT 검색
	@Query(value = "SELECT * FROM stadium_times WHERE MATCH(cron) AGAINST(:cronDay IN BOOLEAN MODE)", nativeQuery = true)
	List<StadiumTime> findByCronDay(@Param("cronDay") String cronDay);
}
