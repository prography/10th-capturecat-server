package com.capturecat.core.api.image.dto;

import java.util.List;

public record UploadItemRequest(String fileName,
								String captureDate,
								List<String> tagNames) {
}
