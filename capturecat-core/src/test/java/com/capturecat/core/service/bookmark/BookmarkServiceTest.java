package com.capturecat.core.service.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.bookmark.Bookmark;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TagRepository tagRepository;

	@InjectMocks
	private BookmarkService bookmarkService;

	@Test
	void 즐겨찾기를_한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willReturn(Optional.of(image));
		given(bookmarkRepository.existsByUserAndImage(user, image))
			.willReturn(false);
		given(bookmarkRepository.save(any()))
			.willReturn(new Bookmark(user, image));

		// when
		bookmarkService.addBookmark(image.getId(), new LoginUser(user));

		// then
		verify(bookmarkRepository).save(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기할_때_회원이_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);

		given(userRepository.findByUsername(anyString()))
			.willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.addBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);
		verify(bookmarkRepository, never()).save(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기를_할_때_이미지가_존재하지_읺으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willThrow(new CoreException(ErrorType.IMAGE_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.addBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);
		verify(bookmarkRepository, never()).save(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기를_할_때_이미_즐겨찾기된_이미지이면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willReturn(Optional.of(image));
		given(bookmarkRepository.existsByUserAndImage(user, image))
			.willReturn(true);

		// when & then
		assertThatThrownBy(() -> bookmarkService.addBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);
		verify(bookmarkRepository, never()).save(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기한_이미지를_조회한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);
		var bookmark = new Bookmark(user, image);

		PageRequest pageRequest = PageRequest.of(0, 10);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(bookmarkRepository.searchBookmarksByUser(eq(user), any(), any()))
			.willReturn(new SliceImpl<>(List.of(bookmark), pageRequest, false));

		// when
		var responses = bookmarkService.getBookmarkImages(new LoginUser(user), null, pageRequest);

		// then
		assertThat(responses.hasNext()).isFalse();
		assertThat(responses.lastCursor()).isNotNull();
		assertThat(responses.items()).hasSize(1);
	}

	@Test
	void 즐겨찾기한_이미지_조회_시_회원이_존재하지_않으면_예외가_발생한다() {
		// given
		var user = DummyObject.newMockUser(1L);

		PageRequest pageRequest = PageRequest.of(0, 10);

		given(userRepository.findByUsername(anyString())).willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.getBookmarkImages(new LoginUser(user), null, pageRequest))
			.isInstanceOf(CoreException.class);
	}

	@Test
	void 즐겨찾기에서_삭제한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);
		var bookmark = DummyObject.newBookmark(user, image);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willReturn(Optional.of(image));
		given(bookmarkRepository.findByUserAndImage(user, image))
			.willReturn(Optional.of(bookmark));
		willDoNothing().given(bookmarkRepository).delete(bookmark);

		// when
		bookmarkService.deleteBookmark(image.getId(), new LoginUser(user));

		// then
		verify(bookmarkRepository).delete(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기_삭제_시_회원이_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);

		given(userRepository.findByUsername(anyString()))
			.willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.deleteBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);

		verify(bookmarkRepository, never()).delete(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기_삭제_시_이미지가_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willThrow(new CoreException(ErrorType.IMAGE_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.deleteBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);

		verify(bookmarkRepository, never()).delete(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기_삭제_시_즐겨찾기가_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);

		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong()))
			.willReturn(Optional.of(image));
		given(bookmarkRepository.findByUserAndImage(user, image))
			.willThrow(new CoreException(ErrorType.BOOKMARK_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> bookmarkService.deleteBookmark(1L, new LoginUser(user)))
			.isInstanceOf(CoreException.class);

		verify(bookmarkRepository, never()).delete(any(Bookmark.class));
	}

	@Test
	void 즐겨찾기한_이미지의_이미지태그를_조회한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var image = DummyObject.newMockImage(1L);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRepository.searchTagsByMemberBookmark(eq(user), any()))
			.willReturn(new SliceImpl<>(List.of(TagFixture.createTag(1L, "tag1")), PageRequest.of(0, 10), false));

		// when
		var responses = bookmarkService.getBookmarkImageTags(new LoginUser(user), PageRequest.of(0, 10));

		// then
		assertThat(responses.hasNext()).isFalse();
		assertThat(responses.lastCursor()).isNotNull();
		assertThat(responses.items()).hasSize(1);
	}
}
