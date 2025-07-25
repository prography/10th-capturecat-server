package com.capturecat.core.service.image;

import static com.capturecat.core.domain.tag.TagFixture.createTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.capturecat.client.upload.FileUploader;
import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.image.dto.ImageInfo;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.tag.TagValidator;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorCode;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	private FileUploader fileUploader;

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private ImageTagRepository imageTagRepository;

	@Mock
	private ImageTagFactory imageTagFactory;

	@Mock
	private TagValidator tagValidator;

	@Mock
	private TagRegister tagRegister;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@InjectMocks
	private ImageService imageService;

	private User user;

	@BeforeEach
	void setUp() {
		user = DummyObject.newUser("testUser");
	}

	@Test
	void getImagesWithTags_hasTagsIsNull() {
		// given
		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.of(user));
		given(imageRepository.searchByUser(eq(user), eq(null), any(Pageable.class)))
			.willReturn(
				new SliceImpl<>(List.of(
					new ImageInfo(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), true,
						List.of(createTag(1L, "tag1"), createTag(2L, "tag2"))))));

		// when
		var response = imageService.getImagesWithTags(new LoginUser(user), null, PageRequest.of(0, 10));

		// then
		assertThat(response.hasNext()).isFalse();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).tags()).hasSize(2);
	}

	@Test
	void getImagesWithTags_hasTagsIsTrue() {
		// given
		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(imageRepository.searchByUser(eq(user), eq(true), any(Pageable.class)))
			.willReturn(new SliceImpl<>(List.of(
				new ImageInfo(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), true,
					List.of(createTag(1L, "tag1"), createTag(2L, "tag2"))))));

		// when
		var response = imageService.getImagesWithTags(new LoginUser(user), true, PageRequest.of(0, 10));

		// then
		assertThat(response.hasNext()).isFalse();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).tags()).hasSize(2);
	}

	@Test
	void getImagesWithTags_hasTagsIsFalse() {
		// given
		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(imageRepository.searchByUser(eq(user), eq(false), any(Pageable.class)))
			.willReturn(new SliceImpl<>(List.of(
				new ImageInfo(1L, "cat.jpg", "http://example.com/cat.jpg", LocalDate.now(), true,
					Collections.emptyList()))));

		// when
		var response = imageService.getImagesWithTags(new LoginUser(user), false, PageRequest.of(0, 10));

		// then
		assertThat(response.hasNext()).isFalse();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).tags()).isEmpty();
	}

	@Test
	void getImagesWithTagsFail() {
		// given
		given(userRepository.findByUsername(anyString()))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageService.getImagesWithTags(new LoginUser(user), false, PageRequest.of(0, 10)))
			.isInstanceOf(CoreException.class)
			.hasMessageContaining(ErrorType.USER_NOT_FOUND.getCode().getMessage());
	}

	@Test
	void removeImage() {
		// given
		var image = DummyObject.newMockUserImage(user);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(imageRepository.findById(anyLong())).willReturn(Optional.of(image));
		willDoNothing().given(fileUploader).delete(eq(image.getFileName()));
		willDoNothing().given(bookmarkRepository).deleteByUserAndImage(any(), any());
		willDoNothing().given(imageTagRepository).deleteAllByImage(any());
		willDoNothing().given(imageRepository).delete(any());

		// when
		imageService.removeImages(1L, new LoginUser(user));

		// then
		verify(fileUploader).delete(eq(image.getFileName()));
		verify(bookmarkRepository).deleteByUserAndImage(eq(user), eq(image));
		verify(imageTagRepository).deleteAllByImage(eq(image));
		verify(imageRepository).delete(eq(image));
	}

	@Test
	void removeImageFail_IsNotOwnerShip() {
		// given
		var anotherUser = DummyObject.newUser("anotherUser");
		var image = DummyObject.newMockUserImage(user);

		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(anotherUser, "id", 2L);
		ReflectionTestUtils.setField(image, "id", 1L);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(anotherUser));
		given(imageRepository.findById(anyLong())).willReturn(Optional.of(image));

		// when & then
		assertThatThrownBy(() -> imageService.removeImages(1L, new LoginUser(anotherUser)))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorCode.IMAGE_ACCESS_DENIED.getMessage());
	}
}
