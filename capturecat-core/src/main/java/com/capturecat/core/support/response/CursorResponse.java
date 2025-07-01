package com.capturecat.core.support.response;

import java.util.List;

public record CursorResponse<T>(boolean hasNext, Long lastCursor, List<T> items) {
}
