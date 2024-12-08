package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stadium_times")
@Getter
@NoArgsConstructor
public class StadiumTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cron;

    @ManyToOne
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    private StadiumTime(String cron, Stadium stadium) {
        this.cron = cron;
        this.stadium = stadium;
    }

    public static StadiumTime createOf(String cron, Stadium stadium) {
        return new StadiumTime(cron, stadium);
    }

    public void updateOf(String cron) {
        this.cron = cron;
    }
}

