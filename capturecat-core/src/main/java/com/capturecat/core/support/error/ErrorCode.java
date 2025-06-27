package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

	EXAMPLE_ERROR("An example error occurred."), NOT_FOUND_IMAGE("이미지를 찾을 수 없어요."),
	EXCEED_MAX_TAG_COUNT("태그는 최대 4개까지만 추가할 수 있어요."), S3_FILE_UPLOAD_FAILED("파일 업로드 과정에서 오류가 발생했어요."),
	INVALID_IMAGE_FORMAT("이미지 파일만 업로드할 수 있어요."), INVALID_REQUEST("잘못된 요청입니다."),
	ALREADY_EXISTS_USERNAME("이미 존재하는 회원 이름이에요."), BEAN_VALIDATION_FAIL("입력 데이터 오류입니다."),
	INVALID_TOKEN("유효하지 않은 JWT 입니다.");
	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}

}
