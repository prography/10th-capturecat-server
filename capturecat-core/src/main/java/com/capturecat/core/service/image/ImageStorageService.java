package com.capturecat.core.service.image;

import com.capturecat.core.domain.image.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageStorageService {
    List<Image> store(List<MultipartFile> files) throws IOException;
//    List<String> findAllByUser(Long userId);
}