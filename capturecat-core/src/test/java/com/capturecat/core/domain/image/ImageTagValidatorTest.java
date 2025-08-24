package com.capturecat.core.domain.image;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class ImageTagValidatorTest {

	@Mock
	private ImageTagRepository imageTagRepository;

	@InjectMocks
	private ImageTagValidator imageTagValidator;

	private Image image;

	@BeforeEach
	void setUp() {
		image = DummyObject.newMockUserImage(1L, 1L);
	}

	@Test
	void 이미지태그_검증() {
		// given
		var tag1 = TagFixture.createTag(1L, "tag1");
		var tag2 = TagFixture.createTag(2L, "tag2");
		var tag3 = TagFixture.createTag(3L, "tag3");
		var tag4 = TagFixture.createTag(4L, "tag4");

		given(imageTagRepository.existsByImageAndTagIn(any(), anyList())).willReturn(false);
		given(imageTagRepository.countByImage(any())).willReturn(0L);

		// when & then
		assertThatNoException().isThrownBy(
			() -> imageTagValidator.validateTags(image, List.of(tag1, tag2, tag3, tag4)));
	}

	@Test
	void 태그가_중복되면_실패한다() {
		// given
		var tag1 = TagFixture.createTag(1L, "tag1");
		var tag2 = TagFixture.createTag(1L, "tag1");

		// when & then
		assertThatThrownBy(() -> imageTagValidator.validateTags(image, List.of(tag1, tag2)))
			.isInstanceOf(CoreException.class)
			.hasMessageContaining(ErrorType.DUPLICATE_TAG_NAMES.getCode().getMessage());
	}

	@Test
	void 이미_등록된_태그를_이미지태그로_등록하면_실패한다() {
		// given
		var tag = TagFixture.createTag(1L, "tag1");

		given(imageTagRepository.existsByImageAndTagIn(any(), anyList())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> imageTagValidator.validateTags(image, List.of(tag)))
			.isInstanceOf(CoreException.class)
			.hasMessageContaining(ErrorType.ALREADY_REGISTERED_TAGS.getCode().getMessage());
	}

	@Test
	void 이미지태그_최대_등록_개수를_초과하면_실패한다() {
		// given
		var tag1 = TagFixture.createTag(1L, "tag1");
		var tag2 = TagFixture.createTag(2L, "tag2");
		var tag3 = TagFixture.createTag(3L, "tag3");
		var tag4 = TagFixture.createTag(4L, "tag4");
		var tag5 = TagFixture.createTag(5L, "tag5");
		var tags = List.of(tag1, tag2, tag3, tag4, tag5);

		// when & then
		assertThatThrownBy(() -> imageTagValidator.validateTags(image, tags))
			.isInstanceOf(CoreException.class)
			.hasMessageContaining(ErrorType.TOO_MANY_TAGS.getCode().getMessage());
	}

	@Test
	void 기존에_등록된_이미지태그_개수와_등록하려는_이미지태그_개수가_최대_등록_개수를_초과하면_실패한다() {
		// given
		var tag = TagFixture.createTag(1L, "tag1");

		given(imageTagRepository.countByImage(any())).willReturn(4L);

		// when & then
		assertThatThrownBy(() -> imageTagValidator.validateTags(image, List.of(tag)))
			.isInstanceOf(CoreException.class)
			.hasMessageContaining(ErrorType.TOO_MANY_TAGS.getCode().getMessage());
	}
}
