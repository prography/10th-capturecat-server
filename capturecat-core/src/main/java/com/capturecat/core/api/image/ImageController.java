package com.capturecat.core.api.image;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(("/v1/images"))
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> upload(@RequestParam List<MultipartFile> files) throws IOException {
        List<Image> images = imageService.save(files);
        List<String> urls = images.stream().map(Image::getFileUrl).toList();
        return ResponseEntity.ok(ApiResponse.success(urls));
    }

    @PostMapping("/{imageId}/tags")
    public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
        imageService.addTagsToImage(imageId, request.tagNames());
        return ApiResponse.success();
    }

}
