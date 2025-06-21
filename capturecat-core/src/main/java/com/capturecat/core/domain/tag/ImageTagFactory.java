package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.stereotype.Component;

import com.capturecat.core.domain.image.Image;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageTagFactory {

	public List<ImageTag> create(Image image, List<Tag> tags) {
		return tags.stream().map(tag -> new ImageTag(image, tag)).toList();
	}

}
