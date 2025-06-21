package com.capturecat.client.upload;

import lombok.Getter;

@Getter
public class UploadException extends RuntimeException {

	private final ErrorCode errorCode;

	public UploadException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

}
