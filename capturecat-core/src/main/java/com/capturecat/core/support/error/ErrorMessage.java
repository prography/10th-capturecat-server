package com.capturecat.core.support.error;

public record ErrorMessage(String code, String message) {

	public ErrorMessage(ErrorType error) {
		this(error.getCode().name(), error.getCode().getMessage());
	}
}
