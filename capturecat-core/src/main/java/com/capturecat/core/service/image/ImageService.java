package com.capturecat.core.service.image;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final FileUploader fileUploader;
    private final ImageRepository imageRepository;

    /**
     * 이미지를 저장하고 저장 위치를 DB에 저장한다.
     * 저장 경로를 브라우저 주소창에 입력하면 이미지가 나타난다.
     */
    public List<Image> save(List<MultipartFile> files) throws IOException {
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

        return imageRepository.saveAll(images);
    }

    private void validate(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
    }
}