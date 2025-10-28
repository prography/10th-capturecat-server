package com.capturecat.core.service.tag;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;

@ExtendWith(MockitoExtension.class)
class ImageTagServiceTest {

	@Mock
	ImageTagRepository imageTagRepository;

	@Mock
	UserRepository userRepository;

	@Mock
	TagRepository tagRepository;

	@InjectMocks
	ImageTagService imageTagService;

	@Test
	void 업데이트_사용자_존재하면_레포지토리_호출한다() {
		// given
		User user = DummyObject.newMockUser(123L);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));

		// when
		imageTagService.update(new LoginUser(user), 10L, 20L);

		// then
		verify(imageTagRepository, times(1))
			.updateImageTagsForUser(user.getId(), 10L, 20L);
	}

	@Test
	void 업데이트_사용자_없으면_CoreException_던진다() {
		// given
		User user = DummyObject.newUser("noUser");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageTagService.update(new LoginUser(user), 1L, 2L))
			.isInstanceOf(CoreException.class);
		verifyNoInteractions(imageTagRepository);
	}

	@Test
	void 삭제_사용자_태그_존재하면_레포지토리_호출한다() {
		// given
		User user = DummyObject.newUser("test");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));

		Tag tag = TagFixture.createTag(10L, "testTag");
		given(tagRepository.findById(anyLong())).willReturn(Optional.of(tag));

		// when
		imageTagService.delete(new LoginUser(user), tag.getId());

		// then
		verify(imageTagRepository, times(1)).deleteByTagAndUser(tag, user);
	}

	@Test
	void 삭제_사용자_없으면_CoreException_던진다() {
		// given
		User user = DummyObject.newUser("noUser");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageTagService.delete(new LoginUser(user), 1L))
			.isInstanceOf(CoreException.class);
		verifyNoInteractions(imageTagRepository, tagRepository);
	}

	@Test
	void 삭제_태그_없으면_CoreException_던진다() {
		// given
		User user = DummyObject.newUser("test");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRepository.findById(anyLong())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageTagService.delete(new LoginUser(user), 2L))
			.isInstanceOf(CoreException.class);
		verifyNoInteractions(imageTagRepository);
	}
}

