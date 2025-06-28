package com.capturecat.core.domain.tag;

import static com.capturecat.core.DummyObject.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class TagValidatorTest {

	private static final String ERROR_TYPE = "errorType";
	@Mock
	private ImageTagRepository imageTagRepository;

	@InjectMocks
	private TagValidator tagValidator;

	private Image image;

	@BeforeEach
	void setUp() {
		image = newMockImage(1L);
	}

	@Test
	void 중복된_태그_이름이_없으면_예외를_발생시키지_않아야_한다() {
		// Given
		List<String> uniqueTagNames = List.of("tag1", "tag2", "tag3");

		// When & Then
		assertThatCode(() -> tagValidator.validateTagNames(image, uniqueTagNames))
			.doesNotThrowAnyException();
	}

	@Test
	void 중복된_태그_이름이_있으면_예외가_발생한다() {
		// Given
		List<String> duplicateTagNames = List.of("tag1", "tag2", "tag1");

		// When & Then
		assertThatThrownBy(() -> tagValidator.validateTagNames(image, duplicateTagNames))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue(ERROR_TYPE, ErrorType.DUPLICATE_TAG_NAMES);
	}

	@Test
	void 이미지가_새로운_태그들을_가지고_있지_않으면_예외를_발생시키지_않아야_한다() {
		// Given
		List<String> newTags = List.of("newTag1", "newTag2");
		given(imageTagRepository.existsByImageAndTagNames(any(), anyList())).willReturn(false);

		// When & Then
		assertThatCode(() -> tagValidator.validateTagNames(image, newTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 이미지가_새로운_태그들_중_하나라도_가지고_있으면_예외가_발생한다() {
		// Given
		List<String> tagsWithExisting = List.of("existingTag", "newTag");
		given(imageTagRepository.existsByImageAndTagNames(any(), anyList())).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> tagValidator.validateTagNames(image, tagsWithExisting))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue(ERROR_TYPE, ErrorType.ALREADY_REGISTERED_TAGS);
	}

	@Test
	void 새로운_태그_목록_크기가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> validTags = List.of("tag1", "tag2", "tag3", "tag4");

		// when & then
		assertThatCode(() -> tagValidator.validateTagCount(image, validTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 새로운_태그_목록_크기가_허용치를_초과하면_예외가_발생한다_예외가_발생한다() {
		// given
		List<String> tooManyTags = List.of("tag1", "tag2", "tag3", "tag4", "tag5");

		// when & then
		assertThatThrownBy(() -> tagValidator.validateTagCount(image, tooManyTags))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue(ERROR_TYPE, ErrorType.TOO_MANY_TAGS);
	}

	@Test
	void 기존_태그와_새로운_태그의_총합이_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2");
		given(imageTagRepository.countByImage(any())).willReturn(2L);

		// when & then
		assertThatCode(() -> tagValidator.validateTagCount(image, newTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 기존_태그와_새로운_태그의_총합이_허용치를_초과하면_예외가_발생한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2");
		given(imageTagRepository.countByImage(any())).willReturn(3L);

		// when & then
		assertThatThrownBy(() -> tagValidator.validateTagCount(image, newTags))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue(ERROR_TYPE, ErrorType.TOO_MANY_TAGS);
	}

	@Test
	void 기존_태그가_없는_경우_새로운_태그_목록의_크기가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2", "newTag3", "newTag4");
		given(imageTagRepository.countByImage(any())).willReturn(0L);

		// when & then
		assertThatCode(() -> tagValidator.validateTagCount(image, newTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 새로운_태그_목록이_비어있고_기존_태그_개수가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> emptyTags = Collections.emptyList();
		given(imageTagRepository.countByImage(any())).willReturn(4L);

		// when & then
		assertThatCode(() -> tagValidator.validateTagCount(image, emptyTags))
			.doesNotThrowAnyException();
	}
}
