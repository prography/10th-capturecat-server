package com.capturecat.core.support.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class CoreExceptionHandler {

	@ExceptionHandler(CoreException.class)
	public ResponseEntity<ApiResponse<?>> handleCoreException(CoreException exception) {
		switch (exception.getErrorType().getLogLevel()) {
			case ERROR -> log.error("CoreException occurred: {}", exception.getMessage(), exception);
			case WARN -> log.warn("CoreException occurred: {}", exception.getMessage(), exception);
			default -> log.info("CoreException occurred: {}", exception.getMessage(), exception);
		}
		return ResponseEntity.status(exception.getErrorType().getStatus())
			.body(ApiResponse.error(exception.getErrorType()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception) {
		log.warn("Validation error occurred: {}", exception.getMessage(), exception);
		return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.INVALID_REQUEST));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleException(Exception exception) {
		log.error("Exception occurred: {}", exception.getMessage(), exception);
		return ResponseEntity.status(ErrorType.DEFAULT_ERROR.getStatus())
			.body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
	}
}
