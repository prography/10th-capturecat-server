package com.capturecat.core.domain.tag;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.annotation.DomainService;
import com.capturecat.core.domain.image.ImageRepository;

@DomainService
@RequiredArgsConstructor
public class ImageTaggingDomainService {

	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final TagRepository tagRepository;
	private final ImageTagFactory imageTagFactory;

	private boolean isNewTag(List<Tag> tags, String tagName) {
		return tags.stream()
			.noneMatch(t -> t.isSameNameAs(tagName));
	}
}
