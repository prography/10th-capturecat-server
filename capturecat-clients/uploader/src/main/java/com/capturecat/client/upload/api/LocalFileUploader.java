package com.capturecat.client.upload.api;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
@Profile({"local", "test"})
@RequiredArgsConstructor
public class LocalFileUploader extends AbstractFileUploader {

    @Value("${image.local.base-path}")
    private String basePath;
    private Path storagePath;

    @PostConstruct
    public void init() throws IOException {
        storagePath = Paths.get(basePath);
        if (!Files.exists(storagePath))
            Files.createDirectories(storagePath);
    }


    @Override
    public String upload(MultipartFile file) throws IOException {
        String savedFileName = buildFileName(file.getOriginalFilename());
        Path destination = storagePath.resolve(savedFileName);

        file.transferTo(destination.toFile());

        return destination.toAbsolutePath().toString();
    }
}
