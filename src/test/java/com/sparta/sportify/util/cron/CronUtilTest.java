package com.sparta.sportify.util.cron;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CronUtilTest {

	@Test
	@DisplayName("크론식 시간이 *일때 모든 시간 추출")
	void extractStartTimes_All() {
		String cronExpr = "0 0 * ? * MON,TUE";

		List<Integer> result = CronUtil.extractStartTimes(cronExpr);

		List<Integer> expected = IntStream.rangeClosed(0, 22).boxed().collect(Collectors.toList());
		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("크론식 시간 추출 성공")
	void extractStartTimes() {
		String cronExpr = "0 0 08-12,13-15,15-17 ? * MON";

		List<Integer> result = CronUtil.extractStartTimes(cronExpr);

		List<Integer> expected = List.of(8, 10, 13, 15);
		assertThat(result).isEqualTo(expected);
	}
}