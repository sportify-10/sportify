package com.sparta.sportify.util;

import java.util.List;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Component;

@Component
public class BadWordFilter {
	private static final List<String> BAD_WORDS = List.of(
		"챗GPT", "백엔드", "멍청이"
	);

	public String containsSimilarBadWord(String input) {
		String[] words = input.split("\\s+");  // 띄어쓰기로 분리
		StringBuilder result = new StringBuilder();

		for (String word : words) {
			boolean isBadWord = false;

			// isSimilar 메서드에서 유사도 계산
			for (String badWord : BAD_WORDS) {
				if (isSimilar(word, badWord)) {
					isBadWord = true;
					break;
				}
			}

			// 비속어가 있으면 ***로 변경
			if (isBadWord) {
				result.append("*** ");
			} else {
				result.append(word + " ");
			}
		}
		return result.toString().trim();  // 맨 끝 공백 제거
	}

	private static boolean isSimilar(String input, String badWord) {
		//특수문자와 숫자 제거
		String normalizedInput = normalize(input);
		String normalizedBadWord = normalize(badWord);

		// 유사도 계산
		JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
		double similarityScore = similarity.apply(normalizedInput, normalizedBadWord);

		// 유사도 70% 이상일 경우 true
		return similarityScore > 0.7;
	}

	private static String normalize(String input) {
		// 특수문자 및 숫자 제거 (한글과 영어만 남기기)
		return input.toLowerCase().replaceAll("[^가-힣a-zㄱ-ㅎ]", "");
	}
}