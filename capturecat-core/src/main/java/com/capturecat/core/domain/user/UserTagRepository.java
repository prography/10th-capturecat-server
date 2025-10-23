package com.capturecat.core.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.capturecat.core.domain.tag.Tag;

public interface UserTagRepository extends JpaRepository<UserTag, Long>, UserTagCustomRepository {

	Optional<UserTag> findByUserAndTag(User user, Tag tag);

	boolean existsByUserAndTag(User user, Tag tag);

	long countByUser(User user);

	@Modifying
	@Query("delete from UserTag ut where ut.user.id = :userId")
	void deleteAllByUserId(@Param("userId") Long userId);
}
