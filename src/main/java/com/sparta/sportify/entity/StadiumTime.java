package com.sparta.sportify.entity;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeCreateRequestDto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "stadium_times")
@Getter
@AllArgsConstructor
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
}

