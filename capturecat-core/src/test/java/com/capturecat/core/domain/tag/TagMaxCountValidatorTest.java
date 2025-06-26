package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class TagMaxCountValidatorTest {

	@Mock
	private ImageTagRepository imageTagRepository;

	@InjectMocks
	private TagMaxCountValidator tagMaxCountValidator;

	private Image image;

	@BeforeEach
	void setUp() {
		image = DummyObject.newMockImage(1L);
	}

	@Test
	void 새로운_태그_목록_크기가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> validTags = List.of("tag1", "tag2", "tag3", "tag4");

		// when & then
		assertThatCode(() -> tagMaxCountValidator.validateTagCount(image, validTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 새로운_태그_목록_크기가_허용치를_초과하면_TOO_MANY_TAGS_예외를_발생시켜야_한다() {
		// given
		List<String> tooManyTags = List.of("tag1", "tag2", "tag3", "tag4", "tag5");

		// when & then
		assertThatThrownBy(() -> tagMaxCountValidator.validateTagCount(image, tooManyTags))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.TOO_MANY_TAGS);
	}

	@Test
	void 기존_태그와_새로운_태그의_총합이_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2");
		given(imageTagRepository.countByImage(any())).willReturn(2L);

		// when & then
		assertThatCode(() -> tagMaxCountValidator.validateTagCount(image, newTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 기존_태그와_새로운_태그의_총합이_허용치를_초과하면_TOO_MANY_TAGS_예외를_발생시켜야_한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2");
		given(imageTagRepository.countByImage(any())).willReturn(3L);

		// when & then
		assertThatThrownBy(() -> tagMaxCountValidator.validateTagCount(image, newTags))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.TOO_MANY_TAGS);
	}

	@Test
	void 기존_태그가_없는_경우_새로운_태그_목록의_크기가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> newTags = List.of("newTag1", "newTag2", "newTag3", "newTag4");
		given(imageTagRepository.countByImage(any())).willReturn(0L);

		// when & then
		assertThatCode(() -> tagMaxCountValidator.validateTagCount(image, newTags))
			.doesNotThrowAnyException();
	}

	@Test
	void 새로운_태그_목록이_비어있고_기존_태그_개수가_허용치_이하면_예외를_발생시키지_않아야_한다() {
		// given
		List<String> emptyTags = Collections.emptyList();
		given(imageTagRepository.countByImage(any())).willReturn(4L);

		// when & then
		assertThatCode(() -> tagMaxCountValidator.validateTagCount(image, emptyTags))
			.doesNotThrowAnyException();
	}
}
