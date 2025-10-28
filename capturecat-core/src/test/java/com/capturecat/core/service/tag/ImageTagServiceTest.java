package com.capturecat.core.service.tag;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.domain.tag.ImageTagRepository;
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

	@InjectMocks
	ImageTagService imageTagService;

	@Test
	void 업데이트_사용자_존재하면_레포지토리_호출한다() {
		// given
		LoginUser loginUser = mock(LoginUser.class);
		when(loginUser.getUsername()).thenReturn("existingUser");

		User user = mock(User.class);
		when(user.getId()).thenReturn(123L);

		when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(user));

		// when
		imageTagService.update(loginUser, 10L, 20L);

		// then
		verify(imageTagRepository, times(1)).updateImageTagsForUser(123L, 10L, 20L);
	}

	@Test
	void 업데이트_사용자_없으면_CoreException_던진다() {
		// given
		LoginUser loginUser = mock(LoginUser.class);
		when(loginUser.getUsername()).thenReturn("noUser");
		when(userRepository.findByUsername("noUser")).thenReturn(Optional.empty());

		// when / then
		assertThrows(CoreException.class, () -> imageTagService.update(loginUser, 1L, 2L));
		verifyNoInteractions(imageTagRepository);
	}
}

