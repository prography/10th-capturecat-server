package com.capturecat.core.api.image;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.support.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/api/v1/images/{imageId}/tags")
    public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
        imageService.addTagsToImage(imageId, request.tagNames());
        return ApiResponse.success();
    }
}
