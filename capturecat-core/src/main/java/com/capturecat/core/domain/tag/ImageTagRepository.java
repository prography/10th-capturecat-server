package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.capturecat.core.domain.image.Image;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

    long countByImage(Image image);

    @Query("SELECT COUNT(it) FROM ImageTag it JOIN it.tag t WHERE it.image = :image AND t.name IN :tagNames")
    long countByImageAndTagNameIn(Image image, List<String> tagNames);

    @Query("SELECT t.name FROM ImageTag it JOIN it.tag t WHERE it.image = :image")
    List<String> findTagNamesByImage(Image image);

    boolean existsByImageAndTag(Image image, Tag tag);
}
