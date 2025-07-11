package com.capturecat.core.service.bookmark;

import java.time.LocalDate;

import com.capturecat.core.domain.image.Image;

public record BookmarkedImageResponse(Long id, String name, String url, LocalDate captureDate) {

	public static BookmarkedImageResponse from(Image image) {
		return new BookmarkedImageResponse(
			image.getId(),
			image.getFileName(),
			image.getFileUrl(),
			image.getCaptureDate()
		);
	}
}
