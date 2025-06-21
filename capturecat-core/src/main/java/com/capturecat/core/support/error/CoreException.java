package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

	private final ErrorType errorType;

	public CoreException(ErrorType errorType) {
		super(errorType.getCode().getMessage());
		this.errorType = errorType;
	}

}
