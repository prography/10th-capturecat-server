package com.capturecat.core.api.search;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.service.search.SearchService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/search")
public class SearchController {

	private final SearchService searchService;

	@GetMapping("/autocomplete")
	public ApiResponse<List<TagResponse>> autocomplete(@AuthenticationPrincipal LoginUser loginUser,
		@RequestParam String keyword,
		@RequestParam(required = false, defaultValue = "10") int size) {
		List<TagResponse> tagResponses = searchService.autocomplete(loginUser, keyword, size);

		return ApiResponse.success(tagResponses);
	}
}
