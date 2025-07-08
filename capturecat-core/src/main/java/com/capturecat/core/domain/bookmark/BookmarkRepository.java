package com.capturecat.core.domain.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	boolean existsByUserAndImage(User user, Image image);
}
