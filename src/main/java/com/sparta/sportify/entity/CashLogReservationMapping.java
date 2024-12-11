package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.metamodel.mapping.MappingType;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
