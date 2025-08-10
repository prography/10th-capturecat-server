package com.capturecat.core.domain.bookmark;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, BookmarkCustomRepository {

	Optional<Bookmark> findByUserAndImage(User user, Image image);

	boolean existsByUserAndImage(User user, Image image);

	void deleteByUserAndImage(User user, Image image);

	void deleteByUserId(Long userId);
}
