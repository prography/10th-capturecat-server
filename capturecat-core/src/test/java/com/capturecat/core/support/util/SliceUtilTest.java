package com.capturecat.core.support.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

class SliceUtilTest {

	@Test
	void 페이지_사이즈_보다_큰_목록이면_hasNext는_true다() {
		// given
		List<Integer> contents = List.of(1, 2, 3, 4, 5);
		Pageable pageable = PageRequest.of(0, 3);

		// when
		Slice<Integer> slice = SliceUtil.toSlice(contents, pageable);

		// then
		assertSoftly(softly -> {
			assertThat(slice.hasNext()).isTrue();
			assertThat(slice.getContent()).containsExactly(1, 2, 3);
		});
	}

	@Test
	void 페이지_사이즈_보다_작거나_같은_목록이면_hasNext는_false다() {
		// given
		List<Integer> contents = List.of(1, 2, 3);
		Pageable pageable = PageRequest.of(0, 3);

		// when
		Slice<Integer> slice = SliceUtil.toSlice(contents, pageable);

		// then
		assertSoftly(softly -> {
			assertThat(slice.hasNext()).isFalse();
			assertThat(slice.getContent()).containsExactly(1, 2, 3);
		});
	}

	@Test
	void 빈_목록이면_빈_Slice를_반환한다() {
		// given
		List<Integer> contents = List.of();
		Pageable pageable = PageRequest.of(0, 3);

		// when
		Slice<Integer> slice = SliceUtil.toSlice(contents, pageable);

		// then
		assertSoftly(softly -> {
			assertThat(slice.hasNext()).isFalse();
			assertThat(slice.getContent()).isEmpty();
		});
	}
}
