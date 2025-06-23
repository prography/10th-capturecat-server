package com.capturecat.core.api.image.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImageAndTagUploadRequest {

	private List<Request> requests;

	@Getter
	@Setter
	public static class Request {

		private MultipartFile file;
		private List<String> tagNames;
	}
}
