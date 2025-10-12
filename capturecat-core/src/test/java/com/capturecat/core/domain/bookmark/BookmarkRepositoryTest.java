package com.capturecat.core.domain.bookmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.UserRepository;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class BookmarkRepositoryTest {

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageTagRepository imageTagRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void 이미지_페치조인_테스트() {
		// given
		var user = userRepository.save(DummyObject.newUser("test"));
		var tag = tagRepository.save(TagFixture.createTag("tag1"));
		var images = imageRepository.saveAll(List.of(
			DummyObject.newMockUserImage(user),
			DummyObject.newMockUserImage(user),
			DummyObject.newMockUserImage(user)
		));
		images.forEach(img -> imageTagRepository.save(new ImageTag(img, tag)));
		var bookmarks = images.stream()
			.map(i -> new Bookmark(user, i))
			.toList();
		bookmarkRepository.saveAll(bookmarks);

		entityManager.flush();
		entityManager.clear();

		// when
		Slice<Bookmark> slice = bookmarkRepository.searchBookmarksByUser(user, null, PageRequest.of(0, 10));

		// then
		assertThat(slice.getContent()).hasSize(3);
		assertThat(slice.hasNext()).isFalse();

		Bookmark firstBookmark = slice.getContent().get(0);
		assertThat(Hibernate.isInitialized(firstBookmark.getImage())).isTrue();
	}

	@Test
	void 태그_파라미터가_존재할_때_해당_태그_이미지만_조회된다() {
		// given
		var user = userRepository.save(DummyObject.newUser("test"));
		var tag1 = tagRepository.save(TagFixture.createTag("tag1"));
		var tag2 = tagRepository.save(TagFixture.createTag("tag2"));
		var image1 = imageRepository.save(DummyObject.newMockUserImage(user));
		var image2 = imageRepository.save(DummyObject.newMockUserImage(user));
		imageTagRepository.save(new ImageTag(image1, tag1));
		imageTagRepository.save(new ImageTag(image2, tag2));
		bookmarkRepository.save(new Bookmark(user, image1));
		bookmarkRepository.save(new Bookmark(user, image2));

		entityManager.flush();
		entityManager.clear();

		// when
		Slice<Bookmark> slice = bookmarkRepository.searchBookmarksByUser(user, tag1, PageRequest.of(0, 10));

		// then
		assertThat(slice.getContent()).hasSize(1);
		assertThat(slice.getContent().get(0).getImage().getId()).isEqualTo(image1.getId());
	}

	@Test
	void 이미지에_태그가_없어도_즐겨찾기_조회된다() {
		// given
		var user = userRepository.save(DummyObject.newUser("test"));
		var imageWithTag = imageRepository.save(DummyObject.newMockUserImage(user));
		var imageWithoutTag = imageRepository.save(DummyObject.newMockUserImage(user));
		var tag = tagRepository.save(TagFixture.createTag("tag1"));
		imageTagRepository.save(new ImageTag(imageWithTag, tag));
		bookmarkRepository.save(new Bookmark(user, imageWithTag));
		bookmarkRepository.save(new Bookmark(user, imageWithoutTag));

		entityManager.flush();
		entityManager.clear();

		// when
		Slice<Bookmark> slice = bookmarkRepository.searchBookmarksByUser(user, null, PageRequest.of(0, 10));

		// then
		assertThat(slice.getContent()).hasSize(2);
		assertThat(slice.getContent()).extracting(b -> b.getImage().getId())
			.containsExactlyInAnyOrder(imageWithTag.getId(), imageWithoutTag.getId());
	}
}
