package com.sparta.sportify.entity.cashLog;

import com.sparta.sportify.entity.reservation.Reservation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "cash_logs_reservation_mapping")
public class CashLogReservationMapping {
    @EmbeddedId
    private CashLogReservationMappingId id;

    @MapsId("cashTransactionId")
    @ManyToOne
    @JoinColumn(name = "cash_logs_id", nullable = false)
    private CashLog cashLog;

    @MapsId("reservationId")
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CashLogReservationMappingType type;
}
