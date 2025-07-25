package com.capturecat.core.api.user;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.user.UserService;
import com.capturecat.test.api.RestDocsTest;

class UserControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/user";
	private static final String ACCESS_TOKEN = "valid-access-token";

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
		given().header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when().post(URL_PREFIX + "/tutorialComplete")
			.then().status(HttpStatus.OK)
			.apply(document("tutorialComplete", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과 (예: SUCCESS)"))));
	}

	@Test
	void 회원_탈퇴() {
		// given
		willDoNothing().given(userService).withdraw(any(LoginUser.class));

		// when & then
		given().header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when().delete(URL_PREFIX + "/withdraw")
			.then().status(HttpStatus.OK)
			.apply(document("withdraw", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과 (예: SUCCESS)"))));
	}

}
