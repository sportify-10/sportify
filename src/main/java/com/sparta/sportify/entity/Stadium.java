package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stadiumName;
    private String location;
    private int aTeamCount;
    private int bTeamCount;
    private String description;
    private int price;
    private String status;
    private boolean deletedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Stadium(String stadiumName, String location,int aTeamCount,int bTeamCount, String description, int price){
        this.stadiumName = stadiumName;
        this.location = location;
        this.aTeamCount = aTeamCount;
        this.bTeamCount = bTeamCount;
        this.description = description;
        this.price = price;
        this.status = "pending";
        this.deletedAt = false;
    }

    public static Stadium createOf(StadiumCreateRequestDto stadiumCreateRequestDto) {
        return new Stadium(
            stadiumCreateRequestDto.getStadiumName(),
            stadiumCreateRequestDto.getLocation(),
            stadiumCreateRequestDto.getTeamACount(),
            stadiumCreateRequestDto.getTeamBCount(),
            stadiumCreateRequestDto.getDescription(),
            stadiumCreateRequestDto.getPrice()
        );
    }
}

