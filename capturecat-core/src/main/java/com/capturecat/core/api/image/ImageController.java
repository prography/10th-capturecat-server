package com.capturecat.core.api.image;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.image.dto.AddTagsToImageRequest;
import com.capturecat.core.api.image.dto.RemoveTagsToImageRequest;
import com.capturecat.core.api.image.dto.UploadItemRequest;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.ImageService;
import com.capturecat.core.service.image.ImageWithTagsResponse;
import com.capturecat.core.support.response.ApiResponse;
import com.capturecat.core.support.response.CursorResponse;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<?> upload(
		@RequestPart List<UploadItemRequest> uploadItems, @RequestPart List<MultipartFile> files,
		@AuthenticationPrincipal LoginUser loginUser) { //접근 권한 permitAll 설정일 경우는 null, 캐스팅 오류x
		imageService.save(uploadItems, files); //SecurityContext에서 직접 꺼내면 'anonymousUser'로 조회되어 캐스팅 오류 발생
		return ApiResponse.success();
	}

	@PostMapping("/{imageId}/tags")
	public ApiResponse<?> addTagsToImage(@PathVariable Long imageId, @RequestBody AddTagsToImageRequest request) {
		imageService.addTagsToImage(imageId, request.tagNames());
		return ApiResponse.success();
	}

	@GetMapping
	public ApiResponse<CursorResponse<ImageWithTagsResponse>> getImagesByUser(
			@PageableDefault(size = 20) Pageable pageable) {
		return ApiResponse.success(imageService.getImagesWithTags(pageable));
	}

	@GetMapping("/search")
	public ApiResponse<CursorResponse<ImageWithTagsResponse>> searchImagesByTags(@RequestParam List<String> tagNames,
		@PageableDefault(size = 20) Pageable pageable) {
		CursorResponse<ImageWithTagsResponse> responses = imageService.searchImagesByTagNames(tagNames, pageable);
		return ApiResponse.success(responses);
	}

	@GetMapping("/{imageId}")
	public ApiResponse<ImageWithTagsResponse> getImageByUser(@PathVariable Long imageId) {
		ImageWithTagsResponse response = imageService.getImageWithTags(imageId);
		return ApiResponse.success(response);
	}

	@DeleteMapping("/{imageId}")
	public ApiResponse<?> removeImageByUser(@PathVariable Long imageId) {
		imageService.removeImages(imageId);
		return ApiResponse.success();
	}

	@DeleteMapping("/{imageId}/tags/{tagId}")
	public ApiResponse<?> removeTagFromImage(@PathVariable Long imageId, @PathVariable Long tagId) {
		imageService.removeTagToImage(imageId, tagId);
		return ApiResponse.success();
	}
}
