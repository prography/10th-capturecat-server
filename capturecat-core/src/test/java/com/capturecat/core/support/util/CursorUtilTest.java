package com.capturecat.core.support.util;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import com.capturecat.core.support.response.CursorResponse;

class CursorUtilTest {

	private DummyItem item1;
	private DummyItem item2;
	private DummyItem item3;

	@BeforeEach
	void setUp() {
		item1 = new DummyItem(1L, "one");
		item2 = new DummyItem(2L, "two");
		item3 = new DummyItem(3L, "three");
	}

	@Test
	void 빈_슬라이스는_empty_응답을_반환한다() {
		// given
		Slice<DummyItem> slice = SliceUtil.toSlice(Collections.emptyList(), PageRequest.of(0, 10));

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(slice, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response).isNotNull();
			softly.assertThat(response.hasNext()).isFalse();
			softly.assertThat(response.lastCursor()).isNull();
			softly.assertThat(response.items()).isEmpty();
		});
	}

	@Test
	void 요소가_하나인_슬라이스는_정상적으로_커서를_반환한다() {
		// given
		Slice<DummyItem> slice = SliceUtil.toSlice(List.of(item1), PageRequest.of(0, 10));

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(slice, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response).isNotNull();
			softly.assertThat(response.hasNext()).isFalse();
			softly.assertThat(response.lastCursor()).isEqualTo(item1.id());
			softly.assertThat(response.items()).containsExactly(item1);
		});
	}

	@Test
	void 여러_요소가_있는_슬라이스는_마지막_요소를_커서로_사용한다() {
		// given
		Slice<DummyItem> slice = SliceUtil.toSlice(List.of(item1, item2, item3), PageRequest.of(0, 10));

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(slice, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response).isNotNull();
			softly.assertThat(response.hasNext()).isFalse();
			softly.assertThat(response.lastCursor()).isEqualTo(item3.id());
			softly.assertThat(response.items()).containsExactly(item1, item2, item3);
		});
	}

	@Test
	void 여러_요소가_있는_슬라이스는_hasNext_가_true일_수_있다() {
		// given
		Slice<DummyItem> slice = SliceUtil.toSlice(List.of(item1, item2, item3), PageRequest.of(0, 2));

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(slice, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response).isNotNull();
			softly.assertThat(response.hasNext()).isTrue();
			softly.assertThat(response.lastCursor()).isEqualTo(item2.id());
			softly.assertThat(response.items()).containsExactly(item1, item2);
		});
	}

	@Test
	void 빈_리스트일_경우_null_커서와_false를_반환한다() {
		// given
		List<DummyItem> items = Collections.emptyList();

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(items, true, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response.hasNext()).isFalse();
			softly.assertThat(response.lastCursor()).isNull();
			softly.assertThat(response.items()).isEmpty();
		});
	}

	@Test
	void 아이템이_있는_경우_마지막_아이템의_ID를_커서로_반환한다() {
		// given
		List<DummyItem> items = List.of(
			new DummyItem(1L, "A"),
			new DummyItem(2L, "B"),
			new DummyItem(3L, "C")
		);

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(items, true, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response.hasNext()).isTrue();
			softly.assertThat(response.lastCursor()).isEqualTo(3L);
			softly.assertThat(response.items()).hasSize(3);
			softly.assertThat(response.items()).containsExactlyElementsOf(items);
		});
	}

	@Test
	void hasNext가_false인_경우_응답에도_false가_반영된다() {
		// given
		List<DummyItem> items = List.of(new DummyItem(100L, "X"));

		// when
		CursorResponse<DummyItem> response = CursorUtil.toCursorResponse(items, false, DummyItem::id);

		// then
		assertSoftly(softly -> {
			softly.assertThat(response.hasNext()).isFalse();
			softly.assertThat(response.lastCursor()).isEqualTo(100L);
		});
	}

	// 내부 테스트용 Dummy DTO
	record DummyItem(Long id, String name) {
	}
}
