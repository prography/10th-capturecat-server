package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
@RequiredArgsConstructor
public class TagMaxCountValidator {

	private static final int TAG_MAX_COUNT = 4;

	private final ImageTagRepository imageTagRepository;

	public void validateTagCount(Image image, List<String> tagNames) {
		validateNewTagListSize(tagNames);
		validateTotalTagCount(image, tagNames);
	}

	private void validateNewTagListSize(List<String> tagNames) {
		if (tagNames.size() > TAG_MAX_COUNT) {
			throw new CoreException(ErrorType.TOO_MANY_TAGS);
		}
	}

	private void validateTotalTagCount(Image image, List<String> tagNames) {
		long existingTagCount = imageTagRepository.countByImage(image);
		if (existingTagCount + tagNames.size() > TAG_MAX_COUNT) {
			throw new CoreException(ErrorType.TOO_MANY_TAGS);
		}
	}
}
