package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.capturecat.core.domain.image.Image;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

    @Query("SELECT t.name FROM ImageTag it JOIN it.tag t WHERE it.image = :image")
    List<String> findTagNamesByImage(Image image);

    @Query("SELECT it FROM ImageTag it JOIN it.tag t WHERE it.image = :image AND t.id IN :tagIds")
    List<ImageTag> findByImageAndTagIds(Image image, List<Long> tagIds);
}
