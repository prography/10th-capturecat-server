package com.capturecat.core.domain.image;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
@RequiredArgsConstructor
public class ImageTagValidator {

	private static final int TAG_MAX_COUNT = 4;

	private final ImageTagRepository imageTagRepository;

	/**
	 * 이미지에 태그를 추가하기 전 모든 유효성 규칙을 검증하는 기본 메서드입니다.
	 */
	public void validateTags(Image image, List<Tag> tags) {
		validateDuplicateTags(tags);
		validateExistingTagsOnImage(image, tags);
		validateTagCount(image, tags);
	}

	/**
	 * 제공된 태그 리스트 내에 중복된 태그가 있는지 확인합니다.
	 */
	private void validateDuplicateTags(List<Tag> tags) {
		Set<Tag> uniqueTags = new HashSet<>(tags);
		if (uniqueTags.size() < tags.size()) {
			throw new CoreException(ErrorType.DUPLICATE_TAG_NAMES);
		}
	}

	/**
	 * 제공된 태그들이 이미지에 이미 등록되어 있는지 확인합니다.
	 */
	private void validateExistingTagsOnImage(Image image, List<Tag> tags) {
		if (tags.isEmpty()) {
			return;
		}

		if (imageTagRepository.existsByImageAndTagIn(image, tags)) {
			throw new CoreException(ErrorType.ALREADY_REGISTERED_TAGS);
		}
	}

	/**
	 * 이미지에 추가될 태그의 개수 관련 규칙만 검증합니다.
	 */
	private void validateTagCount(Image image, List<Tag> tags) {
		int newTagsCount = tags.size();
		if (newTagsCount > TAG_MAX_COUNT) {
			throw new CoreException(ErrorType.TOO_MANY_TAGS);
		}

		long existingTagCount = imageTagRepository.countByImage(image);
		if (existingTagCount + newTagsCount > TAG_MAX_COUNT) {
			throw new CoreException(ErrorType.TOO_MANY_TAGS);
		}
	}
}
