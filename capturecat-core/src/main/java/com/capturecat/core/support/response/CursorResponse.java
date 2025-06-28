package com.capturecat.core.support.response;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Slice;

public record CursorResponse<T>(boolean hasNext, Long lastCursor, List<T> items) {

	public static <T> CursorResponse<T> of(Slice<T> items, Long lastCursor) {
		return new CursorResponse<>(items.hasNext(), lastCursor, items.toList());
	}

	public static <T> CursorResponse<T> empty() {
		return new CursorResponse<>(false, null, Collections.emptyList());
	}
}
