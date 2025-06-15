package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.stereotype.Component;

import com.capturecat.core.domain.image.Image;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImageTagFactory {

    private final ImageTagRepository imageTagRepository;

    public void create(Image image, List<Tag> tags) {
        List<ImageTag> imageTags = tags.stream()
                .filter(tag -> !imageTagRepository.existsByImageAndTag(image, tag))
                .map(tag -> new ImageTag(image, tag))
                .toList();
        imageTagRepository.saveAll(imageTags);
    }
}
