package com.capturecat.core.api.image;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.service.image.ImageStorageService;
import com.capturecat.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> upload(@RequestParam List<MultipartFile> files) throws IOException {
        List<Image> images = imageStorageService.store(files);
        List<String> urls = images.stream().map(Image::getFileUrl).toList();
        return ResponseEntity.ok(ApiResponse.success(urls));
    }
}
