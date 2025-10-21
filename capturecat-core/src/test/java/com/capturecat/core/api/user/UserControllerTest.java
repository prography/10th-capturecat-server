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

import com.capturecat.core.api.user.dto.UserReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.WithdrawReqDto;
import com.capturecat.core.api.user.dto.UserRespDto;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserSettings;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;
import com.capturecat.test.api.RestDocsTest;

class UserControllerTest extends RestDocsTest {

	private static final String URL_PREFIX = "/v1/user";
	private static final String ACCESS_TOKEN = "valid-access-token";
	private static final String REFRESH_TOKEN = "valid-refresh-token";

	private final ObjectMapper om = new ObjectMapper();

	private UserController userController;

	private UserService userService;

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		tokenService = mock(TokenService.class);
		userController = new UserController(userService, tokenService);
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
		WithdrawReqDto requestBody = new WithdrawReqDto();
		requestBody.setReason("test reason");
		willReturn("소셜(google) 연결 해지 성공").given(userService).withdraw(any(LoginUser.class), any());

		// when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.header(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + REFRESH_TOKEN)
			.contentType(ContentType.JSON)
			.body(requestBody)
			.when()
			.delete(URL_PREFIX + "/withdraw")
			.then()
			.log().all() //요청/응답 전체 출력
			.status(HttpStatus.OK)
			.apply(document("withdraw", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("유효한 Refresh 토큰")),
				requestFields(
					fieldWithPath("reason").type(JsonFieldType.STRING).description("탈퇴 사유")),
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

	@Test
	void 설정_정보_조회() {
		willReturn(UserSettings.init(1L)).given(userService).getUserSettings(any());

		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.when()
			.get(URL_PREFIX + "/settings")
			.then()
			.status(HttpStatus.OK)
			.apply(document("getUserSettings", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (예: SUCCESS)"),
					fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
					fieldWithPath("data.screenshotAutoDeleteEnabled").type(JsonFieldType.BOOLEAN)
						.description("스크린샷 자동 삭제 활성화")
				)));
	}

	@Test
	void 설정_정보_변경() {
		UserReqDto.UserSettingsReqDto requestBody = new UserReqDto.UserSettingsReqDto();
		requestBody.setScreenshotAutoDeleteEnabled(true);
		UserSettings userSettings = UserSettings.init(1L);
		userSettings.changeAutoDelete(true);
		willReturn(userSettings).given(userService).setUserSettings(nullable(String.class), anyBoolean());

		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + ACCESS_TOKEN)
			.contentType(ContentType.JSON)
			.body(requestBody)
			.when()
			.put(URL_PREFIX + "/settings")
			.then()
			.status(HttpStatus.OK)
			.apply(document("updateUserSettings", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("유효한 Access 토큰")),
				requestFields(
					fieldWithPath("screenshotAutoDeleteEnabled").type(JsonFieldType.BOOLEAN)
						.description("스크린샷 자동 삭제 활성화")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (예: SUCCESS)"),
					fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
					fieldWithPath("data.screenshotAutoDeleteEnabled").type(JsonFieldType.BOOLEAN)
						.description("스크린샷 자동 삭제 활성화")
				)));
	}
}
