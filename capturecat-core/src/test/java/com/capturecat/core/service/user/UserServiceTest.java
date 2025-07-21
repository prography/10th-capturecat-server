package com.capturecat.core.service.user;

import static com.capturecat.core.DummyObject.*;
import static com.capturecat.core.api.user.dto.UserReqDto.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

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
}
