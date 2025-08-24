package com.capturecat.core.domain.image.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record ImageSaveRequest(
	String fileName,
	String fileUrl,
	long size,
	LocalDate captureDate,
	List<String> tagNames) {
}
