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
	public ResponseEntity<ApiResponse<?>> handleCoreException(CoreException e) {
		switch (e.getErrorType().getLogLevel()) {
			case ERROR -> log.error("CoreException occurred: {}", e.getMessage(), e);
			case WARN -> log.warn("CoreException occurred: {}", e.getMessage(), e);
			default -> log.info("CoreException occurred: {}", e.getMessage(), e);
		}
		return ResponseEntity.status(e.getErrorType().getStatus()).body(ApiResponse.error(e.getErrorType()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("Validation error occurred: {}", e.getMessage(), e);
		return ResponseEntity.badRequest().body(ApiResponse.error(ErrorType.INVALID_REQUEST));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
		log.error("Exception occurred: {}", e.getMessage(), e);
		return ResponseEntity.status(ErrorType.DEFAULT_ERROR.getStatus())
			.body(ApiResponse.error(ErrorType.DEFAULT_ERROR));
	}

}
