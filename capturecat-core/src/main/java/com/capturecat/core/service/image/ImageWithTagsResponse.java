package com.capturecat.core.service.image;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.dto.ImageInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageWithTagsResponse(Long id,
									String name,
									String url,
									LocalDate captureDate,
									boolean isBookmarked,
									List<TagResponse> tags) {

	public static ImageWithTagsResponse from(ImageInfo imageInfo) {
		return new ImageWithTagsResponse(imageInfo.id(), imageInfo.fileName(), imageInfo.fileUrl(),
			imageInfo.captureDate(), imageInfo.isBookmarked(), TagResponse.from(imageInfo.tags()));
	}

	public static ImageWithTagsResponse from(Image image) {
		return new ImageWithTagsResponse(image.getId(), image.getFileName(), image.getFileUrl(),
			image.getCaptureDate(), true, null);
	}
}
