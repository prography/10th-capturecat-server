package com.capturecat.core.domain.image;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.bookmark.Bookmark;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.UserRepository;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class ImageRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageTagRepository imageTagRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void searchByUser() {
		// given
		var user = userRepository.save(DummyObject.newUser("testUser"));
		var image1 = imageRepository.save(DummyObject.newMockUserImage(user));
		var image2 = imageRepository.save(DummyObject.newMockUserImage(user));
		Tag tag1 = tagRepository.save(new Tag("tag1"));
		Tag tag2 = tagRepository.save(new Tag("tag2"));
		imageTagRepository.save(new ImageTag(image1, tag1));
		imageTagRepository.save(new ImageTag(image1, tag2));
		imageTagRepository.save(new ImageTag(image2, tag1));
		imageTagRepository.save(new ImageTag(image2, tag2));
		bookmarkRepository.save(new Bookmark(user, image1));

		entityManager.flush();
		entityManager.clear();

		// when
		var response = imageRepository.searchByUser(user, PageRequest.of(0, 10));

		// then
		assertThat(response.getContent()).hasSize(2);
		assertThat(response.getContent().get(0).isBookmarked()).isFalse();
		assertThat(response.getContent().get(1).isBookmarked()).isTrue();
	}
}
