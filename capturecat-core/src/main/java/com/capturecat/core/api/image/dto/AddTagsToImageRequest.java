package com.capturecat.core.api.image.dto;

import java.util.List;

public record AddTagsToImageRequest(List<String> tagNames) {
}
