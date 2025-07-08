package com.capturecat.client.upload;

import lombok.Getter;

@Getter
public class DeleteException extends RuntimeException {

	private final ErrorCode errorCode;

	public DeleteException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
