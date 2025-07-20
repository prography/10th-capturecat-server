package com.capturecat.core.api.user;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.service.auth.LoginUser;
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
		willDoNothing().given(userService).updateTutorialCompleted(any(LoginUser.class));

		// when & then
		given().contentType(ContentType.JSON).log().all()
			.when().post(URL_PREFIX + "/tutorialComplete")
			.then().status(HttpStatus.OK)
			.apply(document("tutorialComplete", requestPreprocessor(), responsePreprocessor(),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}

}
