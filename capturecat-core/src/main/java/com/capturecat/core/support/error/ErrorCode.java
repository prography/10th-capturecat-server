package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

    EXAMPLE_ERROR("An example error occurred.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
