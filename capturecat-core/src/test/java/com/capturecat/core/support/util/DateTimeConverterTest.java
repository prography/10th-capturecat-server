package com.capturecat.core.support.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorCode;

class DateTimeConverterTest {

	@Test
	void 문자열을_LocalDate로_변환한다() {
		// given
		String localDate = "2025-07-06";

		// when
		LocalDate result = DateTimeConverter.convert(localDate);

		// then
		assertThat(result).isEqualTo(LocalDate.of(2025, 7, 6));
	}

	@Test
	void 날짜_형식이_잘못되면_예외가_발생한다() {
		// given
		String localDate = "2025/07/06";

		// when & then
		assertThatThrownBy(() -> DateTimeConverter.convert(localDate))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorCode.INVALID_DATE_FORMAT.getMessage());
	}
}
