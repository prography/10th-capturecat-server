package com.capturecat.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorType {

    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXAMPLE_ERROR, LogLevel.ERROR),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.IMAGE_NOT_FOUND, LogLevel.WARN);

    private final HttpStatus status;

    private final ErrorCode code;

    private final LogLevel logLevel;

    ErrorType(HttpStatus status, ErrorCode code, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.logLevel = logLevel;
    }
}
