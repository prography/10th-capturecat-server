package com.capturecat.core.service.user;

import static com.capturecat.core.api.user.dto.UserReqDto.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends DummyObject {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

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
}
