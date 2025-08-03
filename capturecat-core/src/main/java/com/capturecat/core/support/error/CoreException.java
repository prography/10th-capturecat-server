package com.capturecat.core.support.error;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class CoreException extends RuntimeException {

	private final ErrorType errorType;
	private String rawResponseBody; //외부 API 호출 응답 결과 (혹은 클라이언트에 전송할 메시지)

	public CoreException(ErrorType errorType) {
		super(errorType.getCode().getMessage());
		this.errorType = errorType;
	}

	public CoreException(ErrorType errorType, String message) {
		super(errorType.getCode().getMessage());
		this.errorType = errorType;
		this.rawResponseBody = message;
	}
}
