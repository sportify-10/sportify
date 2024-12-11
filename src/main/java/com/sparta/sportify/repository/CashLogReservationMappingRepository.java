package com.sparta.sportify.repository;

import com.sparta.sportify.entity.CashLogReservationMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogReservationMappingRepository extends JpaRepository<CashLogReservationMapping, Long> {
}