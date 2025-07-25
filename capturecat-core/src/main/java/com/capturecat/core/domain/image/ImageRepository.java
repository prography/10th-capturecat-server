package com.capturecat.core.domain.image;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capturecat.core.domain.user.User;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageCustomRepository {
	List<Image> findByUser(User user);
}
