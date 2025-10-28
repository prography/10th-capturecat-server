package com.capturecat.core.api.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.TagRenameRequest;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.service.tag.ImageTagService;
import com.capturecat.core.service.user.UserTagService;
import com.capturecat.core.support.response.ApiResponse;
import com.capturecat.core.support.response.CursorResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user-tags")
public class UserTagController {

	private final UserTagService userTagService;
	private final ImageTagService imageTagService;

	@PostMapping
	public ApiResponse<TagResponse> create(@AuthenticationPrincipal LoginUser loginUser, @RequestParam String tagName) {
		TagResponse tagResponse = userTagService.create(loginUser, tagName);

		return ApiResponse.success(tagResponse);
	}

	@GetMapping
	public ApiResponse<CursorResponse<TagResponse>> getAll(@AuthenticationPrincipal LoginUser loginUser,
		@PageableDefault Pageable pageable) {
		CursorResponse<TagResponse> tagResponse = userTagService.getAll(loginUser, pageable);

		return ApiResponse.success(tagResponse);
	}

	@PatchMapping
	public ApiResponse<TagResponse> update(
		@AuthenticationPrincipal LoginUser loginUser,
		@RequestBody TagRenameRequest request
	) {
		TagResponse response = userTagService.update(loginUser, request.currentTagId(), request.newTagName());
		imageTagService.update(loginUser, request.currentTagId(), response.id());

		return ApiResponse.success(response);
	}

	@DeleteMapping
	public ApiResponse<?> delete(@AuthenticationPrincipal LoginUser loginUser, @RequestParam Long tagId) {
		userTagService.delete(loginUser, tagId);

		return ApiResponse.success();
	}
}
