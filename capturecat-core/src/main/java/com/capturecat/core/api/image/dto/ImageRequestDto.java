package com.capturecat.core.api.image.dto;

import java.util.List;

import com.capturecat.core.domain.image.dto.ImageSaveRequest;
import com.capturecat.core.domain.tag.Tag;

public class ImageRequestDto {

	public record UploadItem(
		String fileName,
		long fileSize,
		String captureDate,
		List<String> tagNames
	) {

	}

	public record ImageCreateData(
		ImageSaveRequest imageSaveRequest,
		List<Tag> tags
	) {

	}
}
