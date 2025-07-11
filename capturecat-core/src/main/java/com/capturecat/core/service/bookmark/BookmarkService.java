package com.capturecat.core.service.bookmark;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.bookmark.Bookmark;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final ImageRepository imageRepository;
	private final UserRepository userRepository;

	@Transactional
	public void addBookmark(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));

		validateBookmarkDuplication(user, image);

		bookmarkRepository.save(new Bookmark(user, image));
	}

	@Transactional
	public void deleteBookmark(Long imageId, LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CoreException(ErrorType.IMAGE_NOT_FOUND));
		Bookmark bookmark = bookmarkRepository.findByUserAndImage(user, image)
			.orElseThrow(() -> new CoreException(ErrorType.BOOKMARK_NOT_FOUND));

		bookmarkRepository.delete(bookmark);
	}

	private void validateBookmarkDuplication(User user, Image image) {
		if (bookmarkRepository.existsByUserAndImage(user, image)) {
			throw new CoreException(ErrorType.BOOKMARK_DUPLICATION);
		}
	}
}
