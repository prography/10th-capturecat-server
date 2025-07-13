package com.capturecat.core.support.util;

import java.util.Collections;
import java.util.List;
import java.util.function.ToLongFunction;

import org.springframework.data.domain.Slice;

import com.capturecat.core.support.response.CursorResponse;

/**
 * 커서 기반 페이지네이션 응답 생성을 위한 유틸리티 클래스
 */
public class CursorUtil {

	private CursorUtil() {
	}

	/**
	 * Slice<T> 객체로부터 CursorResponse<T>를 생성합니다.
	 * Slice가 비어 있으면 빈 리스트와 null 커서를 가진 응답을 반환합니다.
	 *
	 * @param slice           페이징 처리된 데이터 슬라이스
	 * @param cursorExtractor 커서로 사용할 값을 추출하는 함수 (보통 엔티티의 ID 등)
	 * @param <T>             응답 아이템의 타입
	 * @return CursorResponse<T> 커서 기반 응답 객체
	 */
	public static <T> CursorResponse<T> toCursorResponse(Slice<T> slice, ToLongFunction<T> cursorExtractor) {
		if (slice.isEmpty()) {
			return new CursorResponse<>(false, null, Collections.emptyList());
		} else {
			List<T> content = slice.toList();
			Long lastCursor = cursorExtractor.applyAsLong(content.getLast());
			return new CursorResponse<>(slice.hasNext(), lastCursor, content);
		}
	}

	/**
	 * List<T>와 hasNext 여부로부터 CursorResponse<T>를 생성합니다.
	 * List가 비어 있으면 빈 리스트와 null 커서를 가진 응답을 반환합니다.
	 *
	 * @param items           응답 아이템 목록
	 * @param hasNext         다음 페이지가 존재하는지 여부
	 * @param cursorExtractor 커서로 사용할 값을 추출하는 함수
	 * @param <T>             응답 아이템의 타입
	 * @return CursorResponse<T> 커서 기반 응답 객체
	 */
	public static <T> CursorResponse<T> toCursorResponse(List<T> items, boolean hasNext,
		ToLongFunction<T> cursorExtractor) {
		if (items.isEmpty()) {
			return new CursorResponse<>(false, null, Collections.emptyList());
		} else {
			Long lastCursor = cursorExtractor.applyAsLong(items.getLast());
			return new CursorResponse<>(hasNext, lastCursor, items);
		}
	}
}
