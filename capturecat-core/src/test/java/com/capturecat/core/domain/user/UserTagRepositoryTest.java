package com.capturecat.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.capturecat.core.DummyObject;
import com.capturecat.core.config.JpaAuditingConfig;
import com.capturecat.core.config.QueryDslConfig;
import com.capturecat.core.domain.tag.Tag;
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
	void findByUserAndTag() {
		// given
		User user = userRepository.save(DummyObject.newUser("test"));
		Tag tag = tagRepository.save(TagFixture.createTag("A"));
		UserTag userTag = userTagRepository.save(UserTag.create(user, tag));

		entityManager.flush();
		entityManager.clear();

		// when
		UserTag result = userTagRepository.findByUserAndTag(user, tag).orElseThrow();

		// then
		assertThat(result.getId()).isEqualTo(userTag.getId());
		assertThat(result.getUser().getId()).isEqualTo(user.getId());
		assertThat(result.getTag().getId()).isEqualTo(tag.getId());
	}
}
