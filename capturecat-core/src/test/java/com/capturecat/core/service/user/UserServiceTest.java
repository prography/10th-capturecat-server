package com.capturecat.core.service.user;

import static com.capturecat.core.DummyObject.*;
import static com.capturecat.core.api.user.dto.UserReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

import com.capturecat.core.api.user.dto.UserRespDto.JoinRespDto;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserSettings;
import com.capturecat.core.domain.user.UserSettingsRepository;
import com.capturecat.core.domain.user.UserSocialAccountRepository;
import com.capturecat.core.domain.user.UserTagRepository;
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

	@Mock
	private UserSocialAccountRepository userSocialAccountRepository;

	@Mock
	private WithdrawLogService withdrawLogService;

	@Mock
	private UserSettingsRepository userSettingsRepository;

	@Mock
	private UserTagRepository userTagRepository;

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
		when(userSettingsRepository.findById(any())).thenReturn(Optional.empty());

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

		// when
		userService.withdraw(new LoginUser(savedUser), "test reason");

		// then
		// 탈퇴 사유 저장
		verify(withdrawLogService).save(savedUser.getId(), "test reason");

		// 즐겨찾기, 이미지, 회원, 설정 정보 삭제
		verify(bookmarkRepository).deleteByUserId(savedUser.getId());
		verify(imageTagRepository).deleteAllTagsByUserId(savedUser.getId());
		verify(imageRepository).deleteAllImagesByUserId(savedUser.getId());
		verify(userSettingsRepository).deleteById(savedUser.getId());
		verify(userTagRepository).deleteAllByUserId(savedUser.getId());

		ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		verify(userRepository).deleteById(captor.capture());
		assertThat(captor.getValue()).isSameAs(savedUser.getId());

		verify(userSettingsRepository).deleteById(savedUser.getId());
	}

	@Test
	void 존재하지_않는_회원_탈퇴시_예외() {
		// given
		User savedUser = newMockUser(1L);

		when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.withdraw(new LoginUser(savedUser), "test reason"))
			.isInstanceOf(CoreException.class)
			.satisfies(e -> {
				CoreException ce = (CoreException)e;
				assertThat(ce.getErrorType()).isEqualTo(ErrorType.USER_NOT_FOUND);
			});
	}

	@Test
	void 설정_정보_조회() {
		User savedUser = newMockUser(1L);
		when(userRepository.findByUsername(savedUser.getUsername())).thenReturn(Optional.of(savedUser));
		when(userSettingsRepository.findById(savedUser.getId()))
			.thenReturn(Optional.of(UserSettings.init(savedUser.getId())));

		UserSettings userSettings = userService.getUserSettings(savedUser.getUsername());

		assertThat(userSettings.getUserId()).isEqualTo(savedUser.getId());
		assertThat(userSettings.isScreenshotAutoDeleteEnabled()).isFalse();
	}

	@Test
	void 설정_정보_변경() {
		User savedUser = newMockUser(1L);
		UserSettings settings = UserSettings.init(savedUser.getId());
		when(userSettingsRepository.findById(savedUser.getId())).thenReturn(Optional.of(settings));
		when(userSettingsRepository.save(settings)).thenReturn(settings);

		assertThat(settings.isScreenshotAutoDeleteEnabled()).isFalse();

		userService.setUserSettings(savedUser.getId(), true);

		assertThat(settings.isScreenshotAutoDeleteEnabled()).isTrue();
	}
}
