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
		imageTagRepository.deleteByTagAndUser(tag, user);

		entityManager.flush();
		entityManager.clear();

		// then
		List<ImageTag> result = imageTagRepository.findByImage(image);
		assertThat(result).isEmpty();
	}

	@Test
	void 한_사용자의_여러_이미지에서_해당_태그가_모두_삭제된다() {
		// given
		var user = userRepository.save(DummyObject.newUser("multiUser"));
		var imageA = imageRepository.save(DummyObject.newMockUserImage(user));
		var imageB = imageRepository.save(DummyObject.newMockUserImage(user));
		var tag = tagRepository.save(TagFixture.createTag("java"));

		imageTagRepository.save(new ImageTag(imageA, tag));
		imageTagRepository.save(new ImageTag(imageB, tag));

		entityManager.flush();
		entityManager.clear();

		// when
		imageTagRepository.deleteByTagAndUser(tag, user);

		entityManager.flush();
		entityManager.clear();

		// then
		assertThat(imageTagRepository.findByImage(imageA)).isEmpty();
		assertThat(imageTagRepository.findByImage(imageB)).isEmpty();
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
		imageTagRepository.deleteByTagAndUser(tag, user1);

		entityManager.flush();
		entityManager.clear();

		// then
		List<ImageTag> user1Result = imageTagRepository.findByImage(image1);
		List<ImageTag> user2Result = imageTagRepository.findByImage(image2);

		assertThat(user1Result).isEmpty();
		assertThat(user2Result).hasSize(1);
		assertThat(user2Result.get(0).getImage().getUser()).isEqualTo(user2);
	}

	@Test
	void 지정한_사용자만_태그가_교체된다() {
		// given
		var user1 = userRepository.save(DummyObject.newUser("user1"));
		var user2 = userRepository.save(DummyObject.newUser("user2"));

		var image1 = imageRepository.save(DummyObject.newMockUserImage(user1));
		var image2 = imageRepository.save(DummyObject.newMockUserImage(user2));

		var oldTag = tagRepository.save(TagFixture.createTag("old"));
		var newTag = tagRepository.save(TagFixture.createTag("new"));

		imageTagRepository.save(new ImageTag(image1, oldTag));
		imageTagRepository.save(new ImageTag(image2, oldTag));

		entityManager.flush();
		entityManager.clear();

		// when
		imageTagRepository.updateImageTagsForUser(user1.getId(), oldTag.getId(), newTag.getId());

		entityManager.flush();
		entityManager.clear();

		// then
		var it1 = imageTagRepository.findByImage(image1).get(0);
		var it2 = imageTagRepository.findByImage(image2).get(0);

		assertThat(it1.getTag().getId()).isEqualTo(newTag.getId());
		assertThat(it2.getTag().getId()).isEqualTo(oldTag.getId());
	}

	@Test
	void 태그가_없는_이미지에는_영향이_없다() {
		// given
		var user = userRepository.save(DummyObject.newUser("noTagUser"));
		var imageWithNoTag = imageRepository.save(DummyObject.newMockUserImage(user));

		var oldTag = tagRepository.save(TagFixture.createTag("old"));
		var newTag = tagRepository.save(TagFixture.createTag("new"));

		var imageWithTag = imageRepository.save(DummyObject.newMockUserImage(user));
		imageTagRepository.save(new ImageTag(imageWithTag, oldTag));

		entityManager.flush();
		entityManager.clear();

		// when
		imageTagRepository.updateImageTagsForUser(user.getId(), oldTag.getId(), newTag.getId());

		entityManager.flush();
		entityManager.clear();

		// then
		assertThat(imageTagRepository.findByImage(imageWithNoTag)).isEmpty();
		var updated = imageTagRepository.findByImage(imageWithTag).get(0);
		assertThat(updated.getTag().getId()).isEqualTo(newTag.getId());
	}
}
