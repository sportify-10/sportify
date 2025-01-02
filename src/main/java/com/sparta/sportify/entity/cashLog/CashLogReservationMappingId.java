package com.sparta.sportify.entity.cashLog;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
public class CashLogReservationMappingId implements Serializable {
    private Long cashTransactionId;
    private Long reservationId;

    // 기본 생성자 추가
    public CashLogReservationMappingId() {
    }

    // Getter & Setter
    public Long getCashTransactionId() {
        return cashTransactionId;
    }

    public void setCashTransactionId(Long cashTransactionId) {
        this.cashTransactionId = cashTransactionId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CashLogReservationMappingId that = (CashLogReservationMappingId) o;
        return Objects.equals(cashTransactionId, that.cashTransactionId) &&
                Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cashTransactionId, reservationId);
    }
}
