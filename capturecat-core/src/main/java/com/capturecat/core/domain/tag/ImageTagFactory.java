package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.image.Image;

@Component
@RequiredArgsConstructor
public class ImageTagFactory {

	public List<ImageTag> create(Image image, List<Tag> tags) {
		return tags.stream().map(tag -> new ImageTag(image, tag)).toList();
	}

}
