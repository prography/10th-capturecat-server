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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(("/v1/images"))
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageMapper mapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageListDto>> upload(List<MultipartFile> files) throws IOException {
        //todo:태그 파싱 (이미지 파일과 태그(텍스트 값)를 같이 보내려면 클라이언트에서는 multipart/form-data 방식밖에 없음)
        List<Image> images = imageService.save(files);

        ImageListDto result = mapper.toDto(images);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{imageId}/tags")
    public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
        imageService.addTagsToImage(imageId, request.tagNames());
        return ApiResponse.success();
    }

}
