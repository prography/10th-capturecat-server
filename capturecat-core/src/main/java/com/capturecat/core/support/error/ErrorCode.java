package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

    EXAMPLE_ERROR("An example error occurred."),
    IMAGE_NOT_FOUND("이미지를 찾을 수 없어요."),
    TAG_LIMIT_EXCEEDED("태그는 최대 4개까지만 추가할 수 있어요.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
