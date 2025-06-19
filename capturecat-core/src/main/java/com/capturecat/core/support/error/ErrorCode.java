package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

    EXAMPLE_ERROR("An example error occurred."),
    NOT_FOUND_IMAGE("이미지를 찾을 수 없어요."),
    EXCEED_MAX_TAG_COUNT("태그는 최대 4개까지만 추가할 수 있어요."),
    INVALID_REQUEST("잘못된 요청입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
