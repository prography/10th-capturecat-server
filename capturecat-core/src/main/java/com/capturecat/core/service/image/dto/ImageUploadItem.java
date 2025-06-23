package com.capturecat.core.service.image.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageUploadItem {

	private MultipartFile file;
	private List<String> tagNames;
}
