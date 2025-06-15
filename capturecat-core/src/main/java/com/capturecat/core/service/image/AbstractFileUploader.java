package com.capturecat.core.service.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public abstract class AbstractFileUploader implements FileUploader {

    /**
     * 저장 path 생성
     * ex)  dev/file1
     */
    protected String buildKey(String dirPrefix, MultipartFile file) {
        String savedFileName = buildFileName(file.getOriginalFilename());
        return String.join("/", dirPrefix, savedFileName);
    }

    /**
     * 저장소에 파일 저장 시 파일명이 중목되지 않도록 난수를 붙인다. URL에 포함된다.
     * DB에 저장되는 파일명은 원본 파일명이다.
     */
    protected String buildFileName(String originalName) {
        return UUID.randomUUID() + "_" + originalName;
    }
}
