package com.capturecat.core.api.image;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.ImageRespDto.ImageListDto;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping(("/v1/images"))
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ImageListDto>> upload(List<MultipartFile> files) {
		// todo:태그 파싱 (이미지 파일과 태그(텍스트 값)를 같이 보내려면 클라이언트에서는 multipart/form-data 방식밖에 없음)
		ImageListDto result = imageService.save(files);

		return ResponseEntity.ok(ApiResponse.success(result));
	}

	@PostMapping("/{imageId}/tags")
	public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
		imageService.addTagsToImage(imageId, request.tagNames());
		return ApiResponse.success();
	}

	@DeleteMapping("/{imageId}/tags")
	public ApiResponse<?> removeTagsFromImage(@PathVariable Long imageId,
			@RequestBody @Valid RemoveTagsToImageRequest request) {
		imageService.removeTagsToImage(imageId, request.tagIds());
		return ApiResponse.success();
	}

}
