package com.capturecat.core.service.image;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("local")
@RequiredArgsConstructor
public class LocalImageStorageService implements ImageStorageService {

    private final ImageRepository imageRepository;

    @Value("${image.local.base-path}")
    private String basePath;

    @Value("${image.local.url-prefix}")
    private String urlPrefix; //클라이언트 접근 가능한 정적 리소스 경로

    @Override
    public List<Image> store(List<MultipartFile> files) throws IOException {
        List<Image> images = new ArrayList<>();
        Path storagePath = Paths.get(basePath);

        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }

            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path target = storagePath.resolve(storedFileName); //기존 경로에 하위 파일 또는 디렉토리를 붙여 새 경로(Path) 만듦

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = urlPrefix + "/" + storedFileName;
            Image image = Image.builder()
                    .fileName(originalFileName)
                    .fileUrl(fileUrl)
                    .size(file.getSize())
                    .build();
            images.add(image);
        }

        return imageRepository.saveAll(images);
    }
}
