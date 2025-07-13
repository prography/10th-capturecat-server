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
import com.capturecat.core.domain.user.UserRepository;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class BookmarkRepositoryTest {

	@Autowired
	private ImageRepository imageRepository;

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
		var images = imageRepository.saveAll(List.of(
			DummyObject.newMockUserImage(user),
			DummyObject.newMockUserImage(user),
			DummyObject.newMockUserImage(user)
		));
		var bookmarks = images.stream()
			.map(i -> new Bookmark(user, i))
			.toList();
		bookmarkRepository.saveAll(bookmarks);

		entityManager.flush();
		entityManager.clear();

		// when
		Slice<Bookmark> slice = bookmarkRepository.searchBookmarksByUser(user, PageRequest.of(0, 10));

		// then
		assertThat(slice.getContent()).hasSize(3);
		assertThat(slice.hasNext()).isFalse();

		Bookmark firstBookmark = slice.getContent().get(0);
		assertThat(Hibernate.isInitialized(firstBookmark.getImage())).isTrue();
	}
}
