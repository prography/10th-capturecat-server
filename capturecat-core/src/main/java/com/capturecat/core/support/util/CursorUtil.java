package com.capturecat.core.support.util;

import java.util.Collections;
import java.util.List;
import java.util.function.ToLongFunction;

import org.springframework.data.domain.Slice;

import com.capturecat.core.support.response.CursorResponse;

public class CursorUtil {

	private CursorUtil() {
	}

	public static <T> CursorResponse<T> toCursorResponse(Slice<T> slice, ToLongFunction<T> cursorExtractor) {
		if (slice.isEmpty()) {
			return new CursorResponse<>(false, null, Collections.emptyList());
		} else {
			List<T> content = slice.toList();
			Long lastCursor = cursorExtractor.applyAsLong(content.getLast());
			return new CursorResponse<>(slice.hasNext(), lastCursor, content);
		}
	}
}
