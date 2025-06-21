package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.capturecat.core.domain.image.Image;

class ImageTagFactoryTest {

	private final ImageTagFactory factory = new ImageTagFactory();

	@Test
	void 이미지와_태그_리스트를_받아_ImageTag_리스트를_생성한다() {
		// given
		Image image = new Image();
		Tag tag1 = new Tag("java");
		Tag tag2 = new Tag("spring");

		List<Tag> tags = List.of(tag1, tag2);

		// when
		List<ImageTag> imageTags = factory.create(image, tags);

		// then
		assertThat(imageTags).hasSize(tags.size());
		assertThat(imageTags).extracting(ImageTag::getTag).containsExactlyInAnyOrder(tag1, tag2);
	}

}
