package com.capturecat.core.domain.tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
@RequiredArgsConstructor
public class TagValidator {

	private static final int TAG_MAX_COUNT = 4;

	private final ImageTagRepository imageTagRepository;

	public void validateTagNames(Image image, List<String> tagNames) {
		validateDuplicateTagNames(tagNames);
		validateExistingTagsOnImage(image, tagNames);
		validateTagCount(image, tagNames);
	}

	private void validateDuplicateTagNames(List<String> tagNames) {
		Set<String> uniqueTagNames = new HashSet<>(tagNames);
		if (uniqueTagNames.size() < tagNames.size()) {
			throw new CoreException(ErrorType.DUPLICATE_TAG_NAMES);
		}
	}

	private void validateExistingTagsOnImage(Image image, List<String> tagNames) {
		if (imageTagRepository.existsByImageAndTagNames(image, tagNames)) {
			throw new CoreException(ErrorType.ALREADY_REGISTERED_TAGS);
		}
	}

	public void validateTagCount(Image image, List<String> tagNames) {
		validateNewTagNamesSize(tagNames);
		validateTotalTagCount(image, tagNames);
	}

	private void validateNewTagNamesSize(List<String> tagNames) {
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
