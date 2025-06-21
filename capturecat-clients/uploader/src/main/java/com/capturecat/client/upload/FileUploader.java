package com.capturecat.client.upload;

import org.springframework.web.multipart.MultipartFile;

//todo: client 모듈로 옮길 것
public interface FileUploader {

	String upload(MultipartFile file);

}
