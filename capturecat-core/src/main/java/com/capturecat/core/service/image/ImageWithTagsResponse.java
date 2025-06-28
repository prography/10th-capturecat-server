package com.capturecat.core.service.image;

import java.util.List;

public record ImageWithTagsResponse(Long id, String name, String url, List<TagResponse> tags) {
}
