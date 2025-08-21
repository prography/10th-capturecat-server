package com.capturecat.client.upload;

import lombok.Getter;

@Getter
public enum ErrorCode {

	S3_UPLOAD_FAILED("S3 업로드 중 오류 발생"),
	S3_DOWNLOAD_FAILED("S3 다운로드 중 오류 발생"),
	S3_DELETE_FAILED("S3 파일 삭제 중 오류 발생"),
	LOCAL_UPLOAD_FAILED("로컬 PC 업로드 중 I/O 오류 발생"),
	LOCAL_DELETE_FAILED("로컬 PC 삭제 중 I/O 오류 발생");

	private final String message;

	ErrorCode(String message) {
		this.message = message;
	}

}
