package com.capturecat.core.service.image;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final int TAG_MAX_COUNT = 4;

    private final ImageRepository imageRepository;
    private final ImageTagRepository imageTagRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void addTagsToImage(Long imageId, List<String> tagNames) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

        long existedTagCount = imageTagRepository.countByImage(image);
        int newTagCount = tagNames.size();
        if (existedTagCount + newTagCount > TAG_MAX_COUNT) {
            throw new CoreException(ErrorType.TOO_MANY_TAGS);
        }

        List<Tag> newTag = tagNames.stream()
                .map(Tag::new)
                .toList();

        List<ImageTag> newImageTags = newTag.stream()
                .map(t -> new ImageTag(image, t))
                .toList();

        tagRepository.saveAll(newTag);
        imageTagRepository.saveAll(newImageTags);
    }
}
