package com.capturecat.core.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capturecat.core.domain.tag.Tag;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {

	boolean existsByUserAndTag(User user, Tag tag);

	long countByUser(User user);
}
