package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.user.UserRepository;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class TagRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageTagRepository imageTagRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void 연관_태그_조회() {
		// given
		var user = userRepository.save(DummyObject.newUser("test"));
		var image1 = imageRepository.save(DummyObject.newMockUserImage(user));
		var image2 = imageRepository.save(DummyObject.newMockUserImage(user));
		var tag1 = tagRepository.save(new Tag("tag1"));
		var tag2 = tagRepository.save(new Tag("tag2"));
		var tag3 = tagRepository.save(new Tag("tag3"));
		var tag4 = tagRepository.save(new Tag("tag4"));

		saveImageTags(image1, List.of(tag1, tag2, tag3));
		saveImageTags(image2, List.of(tag1, tag2, tag4));

		entityManager.flush();
		entityManager.clear();

		// when
		var tags = tagRepository.searchByRelatedTags(user, List.of("tag1", "tag2"), PageRequest.of(0, 10));

		// then
		assertThat(tags.getContent()).hasSize(2);
		assertThat(tags.hasNext()).isFalse();
		assertThat(tags.getContent()).extracting(Tag::getName)
			.containsExactly(tag3.getName(), tag4.getName());
	}

	private void saveImageTags(Image image1, List<Tag> tags) {
		for (Tag tag : tags) {
			imageTagRepository.save(new ImageTag(image1, tag));
		}
	}
}
