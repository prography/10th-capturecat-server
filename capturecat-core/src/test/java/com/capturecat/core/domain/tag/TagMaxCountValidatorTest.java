package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

class TagMaxCountValidatorTest {

	private TagMaxCountValidator validator = new TagMaxCountValidator();

	@Test
	void 태그_총개수가_최대값과_같으면_정상적으로_통과된다() {
		// given
		Set<String> existingTags = Set.of("tag1", "tag2");
		List<String> newTags = List.of("tag3", "tag4");

		// when & then
		assertThatCode(() -> validator.validate(existingTags, newTags)).doesNotThrowAnyException();
	}

	@Test
	void 신규_태그가_없으면_정상적으로_통과된다() {
		// given
		Set<String> existingTags = Set.of("tag1", "tag2");
		List<String> newTags = List.of();

		// when & then
		assertThatCode(() -> validator.validate(existingTags, newTags)).doesNotThrowAnyException();
	}

	@Test
	void 기존_태그가_없고_신규_태그만으로_최대값일_경우_통과된다() {
		// given
		Set<String> existingTags = Set.of();
		List<String> newTags = List.of("tag1", "tag2", "tag3", "tag4");

		// when & then
		assertThatCode(() -> validator.validate(existingTags, newTags)).doesNotThrowAnyException();
	}

	@Test
	void 신규_태그에_중복이_있어도_한번만_카운트된다() {
		// given
		Set<String> existingTags = Set.of("tag1");
		List<String> newTags = List.of("tag2", "tag2", "tag3");

		// when & then
		assertThatCode(() -> validator.validate(existingTags, newTags)).doesNotThrowAnyException();
	}

	@Test
	void 신규_태그가_기존_태그에_포함되어_있으면_카운트되지_않는다() {
		// given
		Set<String> existingTags = Set.of("tag1", "tag2");
		List<String> newTags = List.of("tag2", "tag3");

		// when & then
		assertThatCode(() -> validator.validate(existingTags, newTags)).doesNotThrowAnyException();
	}

	@Test
	void 태그_총개수가_최대값을_초과하면_예외가_발생한다() {
		// given
		Set<String> existingTags = Set.of("tag1", "tag2", "tag3");
		List<String> newTags = List.of("tag4", "tag5");

		// when & then
		assertThatThrownBy(() -> validator.validate(existingTags, newTags)).isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.TOO_MANY_TAGS);
	}

}
