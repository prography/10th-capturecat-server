package com.capturecat.core.api.image;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.ImageAndTagUploadRequest;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping(("/v1/images"))
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<?> upload(@ModelAttribute ImageAndTagUploadRequest request) {
		imageService.save(request.getUploadItems());
		return ApiResponse.success();
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
