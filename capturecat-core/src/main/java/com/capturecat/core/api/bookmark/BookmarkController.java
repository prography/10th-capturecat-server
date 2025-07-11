package com.capturecat.core.api.bookmark;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.bookmark.BookmarkService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/bookmarks")
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@PostMapping
	public ApiResponse<?> addBookmark(@RequestParam Long imageId, @AuthenticationPrincipal LoginUser loginUser) {
		bookmarkService.addBookmark(imageId, loginUser);
		return ApiResponse.success();
	}

	@DeleteMapping
	public ApiResponse<?> deleteBookmark(@RequestParam Long imageId, @AuthenticationPrincipal LoginUser loginUser) {
		bookmarkService.deleteBookmark(imageId, loginUser);
		return ApiResponse.success();
	}
}
