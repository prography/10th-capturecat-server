package com.capturecat.core.service.image;

import java.time.LocalDate;
import java.util.List;

import com.capturecat.core.domain.image.dto.ImageInfo;

public record ImageWithTagsResponse(Long id, String name, String url, LocalDate captureDate, List<TagResponse> tags) {

	public static ImageWithTagsResponse of(ImageInfo imageInfo) {
		return new ImageWithTagsResponse(imageInfo.id(), imageInfo.fileName(), imageInfo.fileUrl(),
			imageInfo.captureDate(), TagResponse.from(imageInfo.tags()));
	}
}
