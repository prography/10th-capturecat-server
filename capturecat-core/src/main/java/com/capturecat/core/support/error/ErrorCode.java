package com.capturecat.core.support.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

	EXAMPLE_ERROR("An example error occurred."),
	NOT_FOUND_IMAGE("이미지를 찾을 수 없어요."),
	EXCEED_MAX_TAG_COUNT("태그는 최대 4개까지만 추가할 수 있어요."),
	S3_FILE_UPLOAD_FAILED("파일 업로드 과정에서 오류가 발생했어요."),
	S3_FILE_DELETE_FAILED("파일을 삭제하는 과정에서 오류가 발생했어요."),
	INVALID_IMAGE_FORMAT("이미지 파일만 업로드할 수 있어요."),
	INVALID_REQUEST("잘못된 요청입니다."),
	UPLOAD_METADATA_MISMATCH("제공된 파일 정보와 업로드 메타데이터가 일치하지 않습니다."),
	DUPLICATE_TAG_NAMES("중복된 태그는 허용되지 않습니다."),
	ALREADY_REGISTERED_TAGS("이미 등록된 태그입니다."),
	BEAN_VALIDATION_FAIL("입력 데이터 오류입니다."),
	INVALID_TOKEN("유효하지 않은 토큰입니다."),
	ALREADY_EXISTS_USERNAME("이미 등록된 회원 이름입니다."),
	UNKNOWN_ROLE("존재하지 않는 권한입니다."),
	NOT_FOUND_USER("사용자를 찾을 수 없습니다."),
	ACCESS_TOKEN_EXPIRED("Access token이 만료됐습니다. 재발급 해주세요."),
	REFRESH_TOKEN_EXPIRED("Refresh token이 만료됐습니다. 다시 로그인해주세요."),
	INVALID_ACCESS_TOKEN("Access token이 유효하지 않습니다."),
	INVALID_REFRESH_TOKEN("Refresh token이 유효하지 않습니다."),
	IMAGE_ACCESS_DENIED("요청하신 이미지에 접근할 권한이 없습니다. 본인 소유가 아니면 접근이 제한됩니다."),
	INVALID_DATE_FORMAT("올바르지 않은 날짜 형식입니다. 날짜는 yyyy-MM-dd 형식으로 입력해주세요."),
	NOT_FOUND_TAG("태그를 찾을 수 없습니다."),
	NOT_FOUND_IMAGE_TAG("이미지에 해당하는 태그를 찾을 수 없습니다."),
	ALREADY_EXISTS_BOOKMARK("이미 즐겨찾기한 이미지입니다."),
	INVALID_ID_TOKEN("유효하지 않은 ID_TOKEN 입니다."),
	INVALID_AUTH_TOKEN("유효하지 않은 AUTH_TOKEN 입니다. apple:authorization_code, kakao:access_token"),
	NOT_FOUND_BOOKMARK("존재하지 않는 즐겨찾기입니다."),
	GENERATE_CLIENT_SECRET_FAIL("Apple client_secret 생성에 실패했습니다."),
	UNLINK_SOCIAL_FAIL("소셜 로그인 연결 해제에 실패했습니다."),
	FETCH_SOCIAL_TOKEN_FAIL("소셜 서비스로부터 idToken 혹은 unlinkKey 획득에 실패했습니다."),
	SOCIAL_API_ERROR("소셜 서비스 API 호출 결과 실패를 응답받았습니다."),
	MISSING_PARAMETER("필수 파라미터 %s(이)가 누락되었습니다."),
	INTERNAL_SERVER_ERROR("서버에서 오류가 발생했습니다."),
	INVALID_LOGOUT_AUTH_TOKEN("ACCESS 토큰 또는 REFRESH 토큰이 유효하지 않습니다."),
	ALREADY_EXISTS_USER_TAG("이미 등록된 유저 태그입니다."),
	EXCEED_MAX_USER_TAG_COUNT("태그는 한 계정당 최대 30개까지 추가할 수 있어요."),
	NOT_FOUND_USER_TAG("유저 태그를 찾을 수 없습니다."),
	NOT_FOUND_USER_SETTINGS("유저 설정 정보를 찾을 수 없습니다."),
	ALREADY_REGISTERED_EMAIL("이미 가입된 이메일입니다.");

	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}

	/**
	 * 동적 인자를 받아 포맷팅된 메시지를 생성합니다.
	 * @param args 메시지 포맷에 들어갈 인자
	 * @return 포맷팅된 최종 오류 메시지
	 */
	public String generateMessage(Object... args) {
		return String.format(this.message, args);
	}
}
