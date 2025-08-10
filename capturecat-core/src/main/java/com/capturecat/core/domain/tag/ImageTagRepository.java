package com.capturecat.core.domain.tag;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

	Optional<ImageTag> findByImageAndTag(Image image, Tag tag);

	@Query("SELECT it FROM ImageTag it JOIN FETCH it.tag t WHERE it.image = :image")
	List<ImageTag> findByImage(Image image);

	@Query("SELECT COUNT(it) > 0 FROM ImageTag it JOIN it.tag t WHERE it.image = :image AND t.name IN :tagNames")
	boolean existsByImageAndTagNames(Image image, List<String> tagNames);

	long countByImage(Image image);

	boolean existsByTag(Tag tag);

	void deleteAllByImage(Image image);

	@Modifying
	@Query("DELETE FROM ImageTag it WHERE it.tag = :tag AND it.image.user = :user")
	void deleteByTagAndUser(Tag tag, User user);
}
