package com.capturecat.core.support.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.querydsl.core.types.Predicate;

class CommonTagQueryConditionsTest {

	@Test
	void 태그_존재_여부_조건을_생성한다() {
		// given
		List<String> tagNames = Arrays.asList("tag1", "tag2");

		// when
		var result = CommonTagQueryConditions.createExistsCondition(tagNames);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getValue()).isNotNull().isInstanceOf(Predicate.class);
	}

	@Test
	void 빈_태그_이름_리스트로_조건을_생성하면_빈_BooleanBuilder를_반환한다() {
		// given
		List<String> tagNames = Collections.emptyList();

		// when
		var result = CommonTagQueryConditions.createExistsCondition(tagNames);

		// then
		assertNotNull(result);
		assertThat(result.getValue()).isNull();
	}
}
