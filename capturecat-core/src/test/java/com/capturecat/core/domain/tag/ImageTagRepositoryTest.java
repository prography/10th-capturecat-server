package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.user.UserRepository;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class ImageTagRepositoryTest {

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
	void 태그를_삭제한다() {
		// given
		var user = userRepository.save(DummyObject.newUser("testUser"));
		var image = imageRepository.save(DummyObject.newMockUserImage(user));
		var tag = tagRepository.save(TagFixture.createTag("java"));
		imageTagRepository.save(new ImageTag(image, tag));

		entityManager.flush();
		entityManager.clear();

		// when
		imageTagRepository.deleteTagAndUser(tag, user);

		entityManager.flush();
		entityManager.clear();

		// then
		List<ImageTag> result = imageTagRepository.findByImage(image);
		assertThat(result).isEmpty();
	}

	@Test
	void 다른_사용자의_태그는_삭제되지_않는다() {
		// given
		var user1 = userRepository.save(DummyObject.newUser("testUser1"));
		var user2 = userRepository.save(DummyObject.newUser("testUser2"));

		var image1 = imageRepository.save(DummyObject.newMockUserImage(user1));
		var image2 = imageRepository.save(DummyObject.newMockUserImage(user2));

		var tag = tagRepository.save(TagFixture.createTag("java"));

		imageTagRepository.save(new ImageTag(image1, tag));
		imageTagRepository.save(new ImageTag(image2, tag));

		entityManager.flush();
		entityManager.clear();

		// when
		imageTagRepository.deleteTagAndUser(tag, user1);

		entityManager.flush();
		entityManager.clear();

		// then
		List<ImageTag> user1Result = imageTagRepository.findByImage(image1);
		List<ImageTag> user2Result = imageTagRepository.findByImage(image2);

		assertThat(user1Result).isEmpty();
		assertThat(user2Result).hasSize(1);
		assertThat(user2Result.get(0).getImage().getUser()).isEqualTo(user2);
	}
}
