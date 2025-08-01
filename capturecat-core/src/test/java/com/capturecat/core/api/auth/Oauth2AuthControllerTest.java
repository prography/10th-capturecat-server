package com.capturecat.core.api.auth;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.capturecat.core.api.auth.dto.SocialLoginRequest;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.SocialService;
import com.capturecat.core.service.auth.SocialService.OidcUserPayload;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;
import com.capturecat.test.api.RestDocsTest;

public class Oauth2AuthControllerTest extends RestDocsTest {

	private static final String REQUEST_PATH = "/v1/auth/{provider}/login";

	private Oauth2AuthController oauth2AuthController;
	private SocialService socialService;
	private TokenService tokenService;
	private UserService userService;

	@BeforeEach
	void setUp() {
		socialService = mock(SocialService.class);
		tokenService = mock(TokenService.class);
		userService = mock(UserService.class);
		oauth2AuthController = new Oauth2AuthController(socialService, userService, tokenService);
		mockMvc = mockController(oauth2AuthController);
	}

	@Test
	void 소셜로그인_성공시_회원가입후_Jwt발행() {
		// given
		String provider = "google";
		String idToken = "test-id-token";
		SocialLoginRequest request = new SocialLoginRequest(idToken, null, null);
		OidcUserPayload payload =
			new OidcUserPayload(provider, "1234", "test@test.com", "testNickname", null, true);
		LoginUser user = buildUser(payload);
		Map<TokenType, String> tokenMap = Map.of(
			TokenType.ACCESS, "access.jwt.token",
			TokenType.REFRESH, "refresh.jwt.token"
		);

		willReturn(payload).given(socialService).verifyAndExtract(provider, idToken, null, null);
		willReturn(user).given(userService).upsertSocialUser(payload);
		willReturn(tokenMap).given(tokenService).issue(user.getUsername(), user.getRole());

		// when & then
		given().contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.when().post(REQUEST_PATH, provider)
			.then().status(HttpStatus.OK)
			.apply(document("socialLogin", requestPreprocessor(), responsePreprocessor(),
				pathParameters(
					parameterWithName("provider").description("소셜 로그인 제공자 (예: google, apple 등)")
				),
				requestFields(
					fieldWithPath("idToken").type(JsonFieldType.STRING)
						.optional()
						.description("클라이언트에서 받은 ID 토큰 (apple은 전송x)"),
					fieldWithPath("nickname").type(JsonFieldType.STRING)
						.optional()
						.description("apple 로그인 시 fullName 별도 전달"),
					fieldWithPath("authToken").type(JsonFieldType.STRING)
						.optional()
						.description("apple 로그인 시 항상 authorization_code 전달, kakao 최초 로그인 시 accessToken 전달")
				),
				responseHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 액세스 토큰"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER).description("Bearer 리프레시 토큰")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("응답 결과 (예: SUCCESS)"),
					fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
					fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("data.tutorialCompleted").type(JsonFieldType.BOOLEAN).description("튜토리얼(시작하기) 완료 여부")
				)));
	}

	private LoginUser buildUser(OidcUserPayload payload) {
		User user = User.builder()
			.email(payload.email())
			.role(UserRole.USER)
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.socialId())
			.nickname(payload.nickname())
			.build();
		return new LoginUser(user);
	}
}
