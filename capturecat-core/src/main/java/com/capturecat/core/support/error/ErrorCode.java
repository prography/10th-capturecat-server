package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

	EXAMPLE_ERROR("An example error occurred."),
	NOT_FOUND_IMAGE("이미지를 찾을 수 없어요."),
	EXCEED_MAX_TAG_COUNT("태그는 최대 4개까지만 추가할 수 있어요."),
	S3_FILE_UPLOAD_FAILED("파일 업로드 과정에서 오류가 발생했어요."),
	INVALID_IMAGE_FORMAT("이미지 파일만 업로드할 수 있어요."),
	INVALID_REQUEST("잘못된 요청입니다."),
	TAG_INFO_MISMATCH("업로드된 이미지 파일에 대한 태그 정보를 찾거나 매칭할 수 없습니다."),
	DUPLICATE_TAG_NAMES("중복된 태그는 허용되지 않습니다."),
	ALREADY_REGISTERED_TAGS("이미 등록된 태그입니다."),
	BEAN_VALIDATION_FAIL("입력 데이터 오류입니다."),
	INVALID_TOKEN("유효하지 않은 토큰입니다."),
	ALREADY_EXISTS_USERNAME("이미 등록된 회원 이름입니다."),
	UNKNOWN_ROLE("존재하지 않는 권한입니다."),
	NOT_FOUND_USER("사용자를 찾을 수 없습니다.");

	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}

}
