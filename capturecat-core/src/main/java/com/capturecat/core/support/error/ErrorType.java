package com.capturecat.core.support.error;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorType {

	DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXAMPLE_ERROR, LogLevel.ERROR),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_IMAGE, LogLevel.WARN),
	TOO_MANY_TAGS(HttpStatus.BAD_REQUEST, ErrorCode.EXCEED_MAX_TAG_COUNT, LogLevel.WARN),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, LogLevel.WARN),
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.S3_FILE_UPLOAD_FAILED, LogLevel.ERROR),
	INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_IMAGE_FORMAT, LogLevel.WARN),
	TAG_INFO_MISMATCH(HttpStatus.BAD_REQUEST, ErrorCode.TAG_INFO_MISMATCH, LogLevel.WARN),
	DUPLICATE_TAG_NAMES(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATE_TAG_NAMES, LogLevel.WARN),
	ALREADY_REGISTERED_TAGS(HttpStatus.BAD_REQUEST, ErrorCode.ALREADY_REGISTERED_TAGS, LogLevel.WARN),
	JOIN_FAIL(HttpStatus.BAD_REQUEST, ErrorCode.ALREADY_EXISTS_USERNAME, LogLevel.WARN),
	LOGIN_FAIL(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, LogLevel.WARN),
	VALIDATION_FAIL(HttpStatus.BAD_REQUEST, ErrorCode.BEAN_VALIDATION_FAIL, LogLevel.WARN),
	INVALID_JWT(HttpStatus.BAD_REQUEST, ErrorCode.BEAN_VALIDATION_FAIL, LogLevel.WARN),
	UNKNOWN_ROLE(HttpStatus.BAD_REQUEST, ErrorCode.BEAN_VALIDATION_FAIL, LogLevel.WARN),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_USER, LogLevel.WARN),
	ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, ErrorCode.ACCESS_TOKEN_EXPIRED, LogLevel.WARN),
	REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, ErrorCode.REFRESH_TOKEN_EXPIRED, LogLevel.WARN),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_ACCESS_TOKEN, LogLevel.WARN),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_REFRESH_TOKEN, LogLevel.WARN),
	IMAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, ErrorCode.IMAGE_ACCESS_DENIED, LogLevel.WARN);

	private final HttpStatus status;

	private final ErrorCode code;

	private final LogLevel logLevel;

	ErrorType(HttpStatus status, ErrorCode code, LogLevel logLevel) {
		this.status = status;
		this.code = code;
		this.logLevel = logLevel;
	}

}
