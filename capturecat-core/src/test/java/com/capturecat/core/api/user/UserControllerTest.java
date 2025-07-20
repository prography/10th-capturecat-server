package com.capturecat.core.api.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.capturecat.core.service.user.UserService;
import com.capturecat.test.api.RestDocsTest;

class UserControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/user";

	private final ObjectMapper om = new ObjectMapper();

	private UserController userController;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		userController = new UserController(userService);
		mockMvc = mockController(userController);
	}

	@Test
	void 튜토리얼_완료_업데이트() {
		// given

		// when & then
	}

}
