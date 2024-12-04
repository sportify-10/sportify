package com.sparta.sportify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cash_log")
public class CashLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cashTransactionId;

    private Integer price;
    private String datetime;
    private Long reservationCode;

    @Enumerated(EnumType.STRING)
    private CashType type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters and Setters
}
