package com.capturecat.core.api.image.dto;

import java.util.List;

public record UploadItemRequest(String fileName,
								String captureDate,
								boolean isBookmarked,
								List<String> tagNames) {
}
