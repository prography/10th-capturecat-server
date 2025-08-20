package com.capturecat.core.domain.tag;

import static com.capturecat.core.domain.tag.TagFixture.createTag;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
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
import com.capturecat.core.domain.user.User;
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

	private User user;

	private Image image1;

	private Image image2;

	private Image image3;

	@BeforeEach
	void setUp() {
		user = userRepository.save(DummyObject.newUser("test"));

		image1 = imageRepository.save(DummyObject.newMockUserImage(user));
		image2 = imageRepository.save(DummyObject.newMockUserImage(user));
		image3 = imageRepository.save(DummyObject.newMockUserImage(user));
	}

	@Test
	void 태그_목록_조회() {
		// given
		var tag1 = tagRepository.save(createTag("tag1"));
		var tag2 = tagRepository.save(createTag("tag2"));
		var tag3 = tagRepository.save(createTag("tag3"));
		var tag4 = tagRepository.save(createTag("tag4"));
		var tag5 = tagRepository.save(createTag("tag5"));

		saveImageTags(image1, List.of(tag1, tag2, tag3));
		saveImageTags(image2, List.of(tag1, tag2, tag4));
		saveImageTags(image3, List.of(tag5));

		entityManager.flush();
		entityManager.clear();

		// when
		var tags = tagRepository.searchUserTagsByUser(user, PageRequest.of(0, 10));

		// then
		assertThat(tags.getContent()).hasSize(5);
		assertThat(tags.hasNext()).isFalse();
		assertThat(tags.getContent()).extracting(Tag::getName)
			.containsExactly(tag5.getName(), tag4.getName(), tag2.getName(), tag1.getName(), tag3.getName());
	}

	@Test
	void 연관_태그_조회() {
		// given
		var tag1 = tagRepository.save(new Tag("tag1"));
		var tag2 = tagRepository.save(new Tag("tag2"));
		var tag3 = tagRepository.save(new Tag("tag3"));
		var tag4 = tagRepository.save(new Tag("tag4"));
		var tag5 = tagRepository.save(new Tag("tag5"));

		saveImageTags(image1, List.of(tag1, tag2, tag3));
		saveImageTags(image2, List.of(tag1, tag2, tag4));
		saveImageTags(image3, List.of(tag5));

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

	@Test
	void 사용자가_가장_많이_사용한_태그_순으로_조회한다() {
		// given
		var tag1 = tagRepository.save(new Tag("tag1"));
		var tag2 = tagRepository.save(new Tag("tag2"));
		var tag3 = tagRepository.save(new Tag("tag3"));

		// tag1은 3개의 이미지에 사용
		imageTagRepository.save(new ImageTag(image1, tag1));
		imageTagRepository.save(new ImageTag(image2, tag1));
		imageTagRepository.save(new ImageTag(image3, tag1));

		// tag2는 2개의 이미지에 사용
		imageTagRepository.save(new ImageTag(image1, tag2));
		imageTagRepository.save(new ImageTag(image2, tag2));

		// tag3는 1개의 이미지에 사용
		imageTagRepository.save(new ImageTag(image1, tag3));

		entityManager.flush();
		entityManager.clear();

		// when
		var result = tagRepository.searchMostUsedTagsByUser(user, PageRequest.of(0, 10));

		// then
		List<Tag> tags = result.getContent();
		assertThat(tags).hasSize(3);
		assertThat(tags.get(0).getName()).isEqualTo(tag1.getName());
		assertThat(tags.get(1).getName()).isEqualTo(tag2.getName());
		assertThat(tags.get(2).getName()).isEqualTo(tag3.getName());
	}

	@Test
	void 키워드_추천() {
		// given
		var tag1 = tagRepository.save(new Tag("자바"));
		var tag2 = tagRepository.save(new Tag("스프링"));
		var tag3 = tagRepository.save(new Tag("자바스크립트"));

		saveImageTags(image1, List.of(tag1));
		saveImageTags(image2, List.of(tag1, tag3));
		saveImageTags(image3, List.of(tag1, tag2, tag3));

		entityManager.flush();
		entityManager.clear();

		// when
		List<Tag> tags = tagRepository.searchByKeyword("자", user.getId(), 10);

		// then
		assertThat(tags).hasSize(2);
		assertThat(tags).extracting(Tag::getName)
			.containsExactlyInAnyOrder(tag1.getName(), tag3.getName());
	}

	private void saveImageTags(Image image1, List<Tag> tags) {
		for (Tag tag : tags) {
			imageTagRepository.save(new ImageTag(image1, tag));
		}
	}
}
