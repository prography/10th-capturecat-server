package com.capturecat.core.domain.image;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.capturecat.core.domain.user.User;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageCustomRepository {
	List<Image> findByUser(User user);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Image i WHERE i.user.id = :userId")
	int deleteAllImagesByUserId(Long userId);
}
