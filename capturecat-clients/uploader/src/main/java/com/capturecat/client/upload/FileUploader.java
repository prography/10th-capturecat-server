package com.capturecat.client.upload;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {

	String upload(MultipartFile file);

}
