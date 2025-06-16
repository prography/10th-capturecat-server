package com.capturecat.core.api.image;

import com.capturecat.core.api.image.dto.ImageMapper;
import com.capturecat.core.api.image.dto.ImageReqDto.ImageUploadDto;
import com.capturecat.core.api.image.dto.ImageRespDto.ImageListDto;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(("/v1/images"))
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper mapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageListDto>> upload(@ModelAttribute ImageUploadDto imageUploadDto) throws IOException {

        List<Image> images = imageService.save(imageUploadDto.getFiles());

        ImageListDto result = mapper.toDto(images);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{imageId}/tags")
    public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
        imageService.addTagsToImage(imageId, request.tagNames());
        return ApiResponse.success();
    }

}
