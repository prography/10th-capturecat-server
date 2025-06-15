package com.capturecat.core.service.image;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
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
    private final ImageTagFactory imageTagFactory;

    // TODO: tagNames 수에 대한 검증
    // TODO: tagNames 중복 제거
    @Transactional
    public void addTagsToImage(Long imageId, List<String> tagNames) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

        Set<String> existingTagNames = new HashSet<>(imageTagRepository.findTagNamesByImage(image));
        List<String> tagsToAdd = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .toList();
        int totalCount = existingTagNames.size() + tagsToAdd.size();
        if (totalCount > TAG_MAX_COUNT) {
            throw new CoreException(ErrorType.TOO_MANY_TAGS);
        }

        List<Tag> newTags = tagsToAdd.stream()
                .map(Tag::new)
                .toList();

        tagRepository.saveAll(newTags);
        List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
        imageTagRepository.saveAll(imageTags);
    }
}
