package com.capturecat.core.domain.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capturecat.core.domain.image.Image;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

    long countByImage(Image image);
}
