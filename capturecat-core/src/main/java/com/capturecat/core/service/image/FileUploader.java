package com.capturecat.core.service.image;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

//todo: client 모듈로 옮길 것
public interface FileUploader {
    String upload(MultipartFile file) throws IOException;
}