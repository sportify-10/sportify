package com.sparta.sportify.entity.match;

import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "matchs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Match {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate date;
	private Integer time;
	private Integer aTeamCount;
	private Integer bTeamCount;

	@ManyToOne
	@JoinColumn(name = "stadium_time_id", nullable = false)
	private StadiumTime stadiumTime;

	public void discountATeamCount(int count) {
		this.aTeamCount -= count;
		if (this.aTeamCount < 0) {
			throw new CustomApiException(ErrorCode.ERR_USER_LIMIT_EXCEEDED);
		}
	}

	public void discountBTeamCount(int count) {
		this.bTeamCount -= count;
		if (this.bTeamCount < 0) {
			throw new CustomApiException(ErrorCode.ERR_USER_LIMIT_EXCEEDED);
		}
	}

	public void addATeamCount(int count) {
		this.aTeamCount += count;
	}

	public void addBTeamCount(int count) {
		this.bTeamCount += count;
	}

	public LocalDateTime getStartTime() {
		String timeString = String.format("%02d:00", time);
		LocalTime localTime = LocalTime.parse(timeString);
		return LocalDateTime.of(date, localTime);
	}

	public LocalDateTime getEndTime() {
		return getStartTime().plusHours(2); // 종료 시간은 시작 시간 + 2시간
	}

	public double getTotalMatchCount() {
		return aTeamCount + bTeamCount;
	}

	public double getTotalStadiumCapacity() {
		return stadiumTime.getStadium().getATeamCount() + stadiumTime.getStadium().getBTeamCount();
	}

	public double getReservationPercentage() {
		double totalCapacity = getTotalStadiumCapacity();
		return totalCapacity > 0 ? (getTotalMatchCount() / totalCapacity) * 100 : 0;
	}
}

