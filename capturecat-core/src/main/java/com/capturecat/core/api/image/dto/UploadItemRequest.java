package com.capturecat.core.api.image.dto;

import java.util.List;

public record UploadItemRequest(String fileName, List<String> tagNames) {
}
