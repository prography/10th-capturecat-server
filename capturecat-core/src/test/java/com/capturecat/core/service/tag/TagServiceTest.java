package com.capturecat.core.service.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.PageRequest.of;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

	@Mock
	private TagRepository tagRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ImageTagRepository imageTagRepository;

	@InjectMocks
	private TagService tagService;

	@Test
	void 가장_많이_사용된_태그를_조회한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);
		var tags = List.of(TagFixture.createTag(1L, "java"), TagFixture.createTag(1L, "spring"));
		var pageRequest = of(0, 10);
		var tagSlice = new SliceImpl<>(tags, pageRequest, false);

		given(userRepository.findByUsername(loginUser.getUsername())).willReturn(Optional.of(user));
		given(tagRepository.searchMostUsedTagsByUser(eq(user), any(Pageable.class))).willReturn(tagSlice);

		// when
		var response = tagService.getMostUsedTags(loginUser, pageRequest);

		// then
		assertThat(response.items()).hasSize(2);
		assertThat(response.items().get(0).name()).isEqualTo("java");
		assertThat(response.items().get(1).name()).isEqualTo("spring");
		assertThat(response.hasNext()).isFalse();

		verify(tagRepository).searchMostUsedTagsByUser(eq(user), any(Pageable.class));
	}

	@Test
	void 가장_많이_사용된_태그를_조회_시_회원이_존재하지_않으면_실패한다() {
		// given
		LoginUser loginUser = new LoginUser(DummyObject.newMockUser(1L));

		given(userRepository.findByUsername(anyString())).willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> tagService.getMostUsedTags(loginUser, of(0, 10)))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND);
	}

	@Test
	void 태그를_삭제한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(loginUser.getUsername())).willReturn(Optional.of(user));
		given(tagRepository.findById(eq(tag.getId()))).willReturn(Optional.of(tag));
		willDoNothing().given(imageTagRepository).deleteTagAndUser(eq(tag), eq(user));

		// when
		var response = tagService.deleteTag(loginUser, tag.getId());

		// then
		assertThat(response.id()).isEqualTo(tag.getId());
		assertThat(response.name()).isEqualTo(tag.getName());

		verify(imageTagRepository).deleteTagAndUser(eq(tag), eq(user));
	}

	@Test
	void 태그_삭제_시_회원이_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(loginUser.getUsername()))
			.willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> tagService.deleteTag(loginUser, tag.getId()))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND);

		verify(imageTagRepository, never()).deleteTagAndUser(eq(tag), eq(user));
	}

	@Test
	void 태그_삭제_시_태그가_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(loginUser.getUsername())).willReturn(Optional.of(user));
		given(tagRepository.findById(eq(tag.getId()))).willThrow(new CoreException(ErrorType.TAG_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> tagService.deleteTag(loginUser, tag.getId()))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.TAG_NOT_FOUND);

		verify(imageTagRepository, never()).deleteTagAndUser(eq(tag), eq(user));
	}
}
