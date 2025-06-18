package com.capturecat.core.service.image;

import com.capturecat.client.upload.FileUploader;
import com.capturecat.core.api.image.dto.ImageMapper;
import com.capturecat.core.api.image.dto.ImageRespDto.ImageListDto;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.*;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



@Service
@RequiredArgsConstructor
public class ImageService {
    private final FileUploader fileUploader;
    private final ImageRepository imageRepository;
    private final ImageTagRepository imageTagRepository;
    private final TagRepository tagRepository;
    private final ImageTagFactory imageTagFactory;
    private final TagMaxCountValidator tagMaxCountValidator;
    private final ImageMapper mapper;

    /**
     * 이미지를 저장하고 저장 위치를 DB에 저장한다.
     * 저장 경로를 브라우저 주소창에 입력하면 이미지가 나타난다.
     */
    @Transactional
    public ImageListDto save(List<MultipartFile> files) throws IOException {
        List<Image> images = new ArrayList<>();

        for (MultipartFile file : files) {
            validate(file);

            String fileUrl = fileUploader.upload(file);

            Image savedImage = Image.builder()
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .size(file.getSize())
                    .build();
            images.add(savedImage);
        }

        imageRepository.saveAll(images);
        return mapper.toDto(images);
    }

    @Transactional
    public void addTagsToImage(Long imageId, List<String> tagNames) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

        Set<String> existingTagNames = new HashSet<>(imageTagRepository.findTagNamesByImage(image));

        tagMaxCountValidator.validate(existingTagNames, tagNames);

        List<Tag> newTags = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .map(Tag::new)
                .toList();

        tagRepository.saveAll(newTags);
        List<ImageTag> imageTags = imageTagFactory.create(image, newTags);
        imageTagRepository.saveAll(imageTags);
    }

    private void validate(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
    }
}