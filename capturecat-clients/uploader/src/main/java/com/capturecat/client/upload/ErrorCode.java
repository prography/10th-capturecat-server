package com.capturecat.client.upload;

import lombok.Getter;

@Getter
public enum ErrorCode {

    S3_UPLOAD_FAILED_IO("S3 업로드 중 I/O 오류 발생"),
    S3_DOWNLOAD_FAILED_SDK("S3 업로드 중 AWS SDK 오류 발생"),
    LOCAL_UPLOAD_FAILED("로컬 PC 업로드 중 I/O 오류 발생");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}