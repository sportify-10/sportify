package com.sparta.sportify.dto.match;

import com.sparta.sportify.entity.matchResult.MatchStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultRequestDto {
	@NotNull(message = "팀 A 점수는 필수입니다.")
	@Positive(message = "팀 A 점수는 양의 정수여야 합니다.")
	private Integer teamAScore;

	@NotNull(message = "팀 B 점수는 필수입니다.")
	@Positive(message = "팀 B 점수는 양의 정수여야 합니다.")
	private Integer teamBScore;

	@NotNull(message = "경기 상태는 필수입니다.")
	private MatchStatus matchStatus;

	@NotNull(message = "경기 ID는 필수입니다.")
	private Long matchId;
}