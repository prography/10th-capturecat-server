package com.capturecat.core.service.tag;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capturecat.core.api.tag.AddTagsToImageRequest;
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
public class TagService {

    private final ImageRepository imageRepository;
    private final ImageTagRepository imageTagRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void addTagsToImage(Long imageId, AddTagsToImageRequest request) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

        List<Tag> newTag = request.tags().stream()
                .map(Tag::new)
                .toList();

        List<ImageTag> newImageTags = newTag.stream()
                .map(t -> new ImageTag(image, t))
                .toList();

        tagRepository.saveAll(newTag);
        imageTagRepository.saveAll(newImageTags);
    }
}
