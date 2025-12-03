package com.capturecat.core.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.capturecat.core.support.error.ErrorMessage;
import com.capturecat.core.support.error.ErrorType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(ResultType result, T data, ErrorMessage error) {

	public static ApiResponse<?> success() {
		return new ApiResponse<>(ResultType.SUCCESS, null, null);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(ResultType.SUCCESS, data, null);
	}

	public static ApiResponse<?> error(ErrorType error) {
		return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error));
	}

	public static ApiResponse<?> error(ErrorType error, Object... args) {
		return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(error, args));
	}

	public static ApiResponse<?> error(ErrorType error, Object detail) {
		return new ApiResponse<>(ResultType.ERROR, detail, new ErrorMessage(error));
	}
}
