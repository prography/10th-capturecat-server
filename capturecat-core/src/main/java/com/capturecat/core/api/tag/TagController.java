package com.capturecat.core.api.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

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
	public ApiResponse<?> getTags(@PageableDefault Pageable pageable) {
		CursorResponse<TagResponse> tags = tagService.getTags(pageable);
		return ApiResponse.success(tags);
	}
}
