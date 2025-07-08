package com.capturecat.client.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Profile({"local", "test"})
@RequiredArgsConstructor
public class LocalFileUploader extends AbstractFileUploader {

	@Value("${image.local.base-path}")
	private String basePath;

	private Path storagePath;

	@PostConstruct
	public void init() throws IOException {
		storagePath = Paths.get(basePath);
		if (!Files.exists(storagePath)) {
			Files.createDirectories(storagePath);
		}
	}

	@Override
	public String upload(MultipartFile file) {
		String savedFileName = buildFileName(file.getOriginalFilename());
		Path destination = storagePath.resolve(savedFileName);

		try {
			file.transferTo(destination.toFile());
		} catch (IOException e) {
			throw new UploadException(ErrorCode.LOCAL_UPLOAD_FAILED, e);
		}
		return destination.toAbsolutePath().toString();
	}

	@Override
	public void delete(String fileName) {
		Path destination = storagePath.resolve(fileName);

		try {
			Files.deleteIfExists(destination);
		} catch (IOException e) {
			throw new UploadException(ErrorCode.LOCAL_UPLOAD_FAILED, e);
		}
	}
}
