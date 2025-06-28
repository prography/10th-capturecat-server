package com.capturecat.core.service.image;

import java.util.List;

import com.capturecat.core.domain.tag.Tag;

public record TagResponse(Long id, String name) {

	public static List<TagResponse> from(List<Tag> tags) {
		return tags.stream()
			.map(tag -> new TagResponse(tag.getId(), tag.getName()))
			.toList();
	}
}
