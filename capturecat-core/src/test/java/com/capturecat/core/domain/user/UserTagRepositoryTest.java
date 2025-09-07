package com.capturecat.core.domain.user;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
class UserTagRepositoryTest {

	@Autowired
	EntityManager entityManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	TagRepository tagRepository;

	@Autowired
	UserTagRepository userTagRepository;

	@Test
	void findAllByUser_ShouldReturnUserTags() {
		// given
		var user = DummyObject.newUser("test");
		userRepository.save(user);

		var tag1 = TagFixture.createTag("tag1");
		var tag2 = TagFixture.createTag("tag2");
		tagRepository.saveAll(List.of(tag1, tag2));

		UserTag userTag1 = UserTag.create(user, tag1);
		UserTag userTag2 = UserTag.create(user, tag2);
		userTagRepository.saveAll(List.of(userTag1, userTag2));

		entityManager.flush();
		entityManager.clear();

		// when
		Slice<UserTag> result = userTagRepository.findAllByUser(user, PageRequest.of(0, 10));

		// then
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent())
			.extracting("tag.name")
			.containsExactlyInAnyOrder("tag1", "tag2");
	}
}
