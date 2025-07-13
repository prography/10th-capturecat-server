package com.capturecat.core.api.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.service.tag.TagService;
import com.capturecat.core.support.response.ApiResponse;
import com.capturecat.core.support.response.CursorResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tags")
public class TagController {

	private final TagService tagService;

	@GetMapping
	public ApiResponse<?> getTags(@AuthenticationPrincipal LoginUser loginUser, @PageableDefault Pageable pageable) {
		CursorResponse<TagResponse> tags = tagService.getTags(loginUser, pageable);
		return ApiResponse.success(tags);
	}

	@GetMapping("/related")
	public ApiResponse<CursorResponse<TagResponse>> getTagsByName(@AuthenticationPrincipal LoginUser loginUser,
			@RequestParam List<String> tagNames,
			@PageableDefault Pageable pageable) {
		CursorResponse<TagResponse> tags = tagService.getRelatedTags(loginUser, tagNames, pageable);
		return ApiResponse.success(tags);
	}
}
