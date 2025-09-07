package com.capturecat.core.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserTag;
import com.capturecat.core.domain.user.UserTagFixture;
import com.capturecat.core.domain.user.UserTagRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class UserTagServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	UserTagRepository userTagRepository;

	@Mock
	TagRegister tagRegister;

	@InjectMocks
	UserTagService userTagService;

	@Test
	void 유저_태그를_생성한다() {
		// given
		var user = DummyObject.newUser("test");
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRegister.registerTagsFor(anyString())).willReturn(tag);
		given(userTagRepository.existsByUserAndTag(eq(user), eq(tag))).willReturn(false);
		given(userTagRepository.countByUser(eq(user))).willReturn(0L);
		given(userTagRepository.save(any())).willReturn(UserTagFixture.createUserTag(1L, user, tag));

		// when
		var response = userTagService.create(new LoginUser(user), "java");

		// then
		assertThat(response.id()).isNotNull();
		assertThat(response.name()).isEqualTo(tag.getName());

		verify(userTagRepository, times(1)).save(any());
	}

	@Test
	void 유저_태그_생성_시_회원이_없으면_실패한다() {
		// given
		given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userTagService.create(new LoginUser(DummyObject.newUser("test")), "java"))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorType.USER_NOT_FOUND.getCode().getMessage());

		verify(userTagRepository, never()).save(any());
	}

	@Test
	void 유저_태그_생성_시_이미_등록된_경우_실패한다() {
		// given
		var user = DummyObject.newUser("test");
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRegister.registerTagsFor(anyString())).willReturn(tag);
		given(userTagRepository.existsByUserAndTag(eq(user), eq(tag))).willReturn(true);

		// when & then
		assertThatThrownBy(() -> userTagService.create(new LoginUser(DummyObject.newUser("test")), "java"))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorType.USER_TAG_ALREADY_EXISTS.getCode().getMessage());

		verify(userTagRepository, never()).save(any());
	}

	@Test
	void 유저_태그_생성_시_최대_등록_개수를_초과하면_실패한다() {
		// given
		var user = DummyObject.newUser("test");
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRegister.registerTagsFor(anyString())).willReturn(tag);
		given(userTagRepository.existsByUserAndTag(eq(user), eq(tag))).willReturn(false);
		given(userTagRepository.countByUser(eq(user))).willReturn(30L);

		// when & then
		assertThatThrownBy(() -> userTagService.create(new LoginUser(DummyObject.newUser("test")), "java"))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorType.TOO_MANY_USER_TAGS.getCode().getMessage());

		verify(userTagRepository, never()).save(any());
	}

	@Test
	void 유저_태그를_조회한다() {
		// given
		var user = DummyObject.newUser("test");
		var tag = TagFixture.createTag(1L, "java");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(userTagRepository.findAllByUser(eq(user), any()))
			.willReturn(new SliceImpl<>(List.of(UserTag.create(user, tag))));

		// when
		var response = userTagService.getAll(new LoginUser(user), PageRequest.of(0, 10));

		// then
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).name()).isEqualTo(tag.getName());
	}

	@Test
	void 유저_태그를_조회_시_사용자가_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newUser("test");

		given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userTagService.getAll(new LoginUser(user), PageRequest.of(0, 10)))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorType.USER_NOT_FOUND.getCode().getMessage());
	}
}
