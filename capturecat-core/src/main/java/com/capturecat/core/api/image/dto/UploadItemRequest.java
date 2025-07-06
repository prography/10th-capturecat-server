package com.capturecat.core.api.image.dto;

import java.time.LocalDate;
import java.util.List;

public record UploadItemRequest(String fileName, LocalDate captureDate, List<String> tagNames) {
}
