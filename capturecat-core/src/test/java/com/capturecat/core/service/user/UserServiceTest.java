package com.capturecat.core.service.user;

import static com.capturecat.core.DummyObject.*;
import static com.capturecat.core.api.user.dto.UserReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private ImageTagRepository imageTagRepository;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Spy
	private PasswordEncoder passwordEncoder;

	@Test
	void 회원가입() {
		//given
		JoinReqDto joinReqDto = new JoinReqDto();
		joinReqDto.setUsername("username1");
		joinReqDto.setPassword("password1");
		joinReqDto.setEmail("username1@email.com");

		when(userRepository.existsByUsername(any())).thenReturn(false);
		when(userRepository.save(any())).thenReturn(newMockUser(1L));

		//when
		JoinRespDto joinRespDto = userService.join(joinReqDto);

		//then
		Assertions.assertEquals(1L, joinRespDto.getId());
	}

	@Test
	void 튜토리얼_완료_업데이트() {
		//given
		//기 회원 만들기
		User savedUser = newMockUser(1L);

		//기본값 false
		Assertions.assertEquals(false, savedUser.isTutorialCompleted());

		when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.of(savedUser));

		//when
		userService.updateTutorialCompleted(new LoginUser(savedUser));

		//then
		Assertions.assertEquals(true, savedUser.isTutorialCompleted());
	}

	@Test
	void 회원탈퇴_성공() {
		//given
		//기 회원 만들기
		User savedUser = newMockUser(1L);

		when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.of(savedUser));

		// User가 소유한 이미지 2개라고 가정
		Image image1 = mock(Image.class);
		Image image2 = mock(Image.class);
		List<Image> userImages = List.of(image1, image2);

		when(imageRepository.findByUser(savedUser)).thenReturn(userImages);

		// when
		userService.withdraw(new LoginUser(savedUser));

		// then
		verify(bookmarkRepository).deleteByUser(savedUser);
		verify(imageRepository).findByUser(savedUser);

		// 각 이미지에 대해 imageTagRepository.deleteAllByImage 호출
		verify(imageTagRepository).deleteAllByImage(image1);
		verify(imageTagRepository).deleteAllByImage(image2);

		// 이미지 전체 삭제
		verify(imageRepository).deleteAll(userImages);

		// 마지막으로 user 삭제
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).delete(captor.capture());
		assertThat(captor.getValue()).isSameAs(savedUser);
	}

	@Test
	void 존재하지_않는_회원_탈퇴시_예외() {
		// given
		//given
		//기 회원 만들기
		User savedUser = newMockUser(1L);

		when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.withdraw(new LoginUser(savedUser)))
			.isInstanceOf(CoreException.class)
			.satisfies(e -> {
				CoreException ce = (CoreException) e;
				assertThat(ce.getErrorType()).isEqualTo(ErrorType.USER_NOT_FOUND);
			});
	}
}
