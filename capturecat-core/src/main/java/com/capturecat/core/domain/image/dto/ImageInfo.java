package com.capturecat.core.domain.image.dto;

import java.util.List;

import com.capturecat.core.domain.tag.Tag;

public record ImageInfo(Long id,
						String fileName,
						String fileUrl,
						List<Tag> tags) {
}
