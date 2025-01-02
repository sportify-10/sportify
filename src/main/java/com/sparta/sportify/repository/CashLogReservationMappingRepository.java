package com.sparta.sportify.repository;

import com.sparta.sportify.entity.cashLog.CashLogReservationMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogReservationMappingRepository extends JpaRepository<CashLogReservationMapping, Long> {

    CashLogReservationMapping findCashLogReservationMappingById(Long id);
}
