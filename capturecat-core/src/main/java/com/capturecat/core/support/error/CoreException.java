package com.capturecat.core.support.error;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class CoreException extends RuntimeException {

	private final ErrorType errorType;

	public CoreException(ErrorType errorType) {
		super(errorType.getCode().getMessage());
		this.errorType = errorType;
	}

	public CoreException(ErrorType errorType, String message) {
		super(errorType.getCode().getMessage());
		this.errorType = errorType;
		log.error(message);
	}
}
