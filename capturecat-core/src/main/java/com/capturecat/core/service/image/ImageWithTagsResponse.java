package com.capturecat.core.service.image;

import java.util.List;

import com.capturecat.core.domain.image.dto.ImageInfo;

public record ImageWithTagsResponse(Long id, String name, String url, List<TagResponse> tags) {

	public static ImageWithTagsResponse of(ImageInfo imageInfo) {
		return new ImageWithTagsResponse(imageInfo.id(), imageInfo.fileName(), imageInfo.fileUrl(),
			TagResponse.from(imageInfo.tags()));
	}
}
