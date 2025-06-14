package com.capturecat.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorType {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXAMPLE_ERROR, LogLevel.ERROR),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_IMAGE, LogLevel.WARN),
    TOO_MANY_TAGS(HttpStatus.BAD_REQUEST, ErrorCode.EXCEED_MAX_TAG_COUNT, LogLevel.WARN);

    private final HttpStatus status;

    private final ErrorCode code;

    private final LogLevel logLevel;

    ErrorType(HttpStatus status, ErrorCode code, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.logLevel = logLevel;
    }
}
