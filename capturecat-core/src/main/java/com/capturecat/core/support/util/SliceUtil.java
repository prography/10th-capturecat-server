package com.capturecat.core.support.util;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public class SliceUtil {

	private SliceUtil() {
	}

	public static <T> Slice<T> toSlice(List<T> contents, Pageable pageable) {
		if (contents.size() > pageable.getPageSize()) {
			return new SliceImpl<>(contents.subList(0, pageable.getPageSize()), pageable, true);
		} else {
			return new SliceImpl<>(contents, pageable, false);
		}
	}
}
