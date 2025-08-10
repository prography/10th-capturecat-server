package com.capturecat.core.api.user;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.http.ContentType;

import com.capturecat.core.api.user.dto.UserRespDto;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.domain.user.User;
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
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
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
		willReturn("소셜(google) 연결 해지 성공").given(userService).withdraw(any(LoginUser.class), any());

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when()
			.delete(URL_PREFIX + "/withdraw")
			.then()
			.log().all() //요청/응답 전체 출력
			.status(HttpStatus.OK)
			.apply(document("withdraw", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과 (예: SUCCESS)"),
					fieldWithPath("data").type(JsonFieldType.STRING).description("소셜 서비스 해지 요청 결과")
				)));
	}

	@Test
	void 사용자_정보_조회() {
		// given
		User user = User.builder()
			.username("test@email.com")
			.nickname("testNickname")
			.build();
		user.tutorialComplete();
		UserRespDto.InfoRespDto infoRespDto = new UserRespDto.InfoRespDto(user);
		willReturn(infoRespDto).given(userService).getUserInfo(any());

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when()
			.get(URL_PREFIX + "/info")
			.then()
			.status(HttpStatus.OK)
			.apply(document("userInfo", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (예: SUCCESS)"),
					fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
					fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("data.tutorialCompleted").type(JsonFieldType.BOOLEAN).description("튜토리얼(시작하기) 완료 여부")
				)));
	}
}
