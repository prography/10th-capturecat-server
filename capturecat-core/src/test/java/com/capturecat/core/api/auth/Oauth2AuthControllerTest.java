package com.capturecat.core.api.auth;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.capturecat.core.api.auth.dto.OauthLoginRequest;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.service.auth.IdTokenVerifierService;
import com.capturecat.core.service.auth.IdTokenVerifierService.OidcUserPayload;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;
import com.capturecat.test.api.RestDocsTest;

public class Oauth2AuthControllerTest extends RestDocsTest {

	private static final String REQUEST_PATH = "/v1/auth/oauth2/login";

	private Oauth2AuthController oauth2AuthController;
	private IdTokenVerifierService idTokenVerifierService;
	private TokenService tokenService;
	private UserService userService;

	@BeforeEach
	void setUp() {
		idTokenVerifierService = mock(IdTokenVerifierService.class);
		tokenService = mock(TokenService.class);
		userService = mock(UserService.class);
		oauth2AuthController = new Oauth2AuthController(idTokenVerifierService, userService, tokenService);
		mockMvc = mockController(oauth2AuthController);
	}

	@Test
	void 소셜로그인_성공시_회원가입후_Jwt발행() {
		// given
		String provider = "google";
		String idToken = "test-id-token";
		OauthLoginRequest request = new OauthLoginRequest(provider, idToken);
		OidcUserPayload payload =
			new OidcUserPayload(provider, "1234", "test@test.com", true);
		LoginUser user = buildUser(payload);
		Map<TokenType, String> tokenMap = Map.of(
			TokenType.ACCESS, "access.jwt.token",
			TokenType.REFRESH, "refresh.jwt.token"
		);

		willReturn(payload).given(idTokenVerifierService).verifyAndExtract(provider, idToken);
		willReturn(user).given(userService).upsertSocialUser(payload);
		willReturn(tokenMap).given(tokenService).issue(user.getUsername(), user.getRole().getValue());

		// when & then
		given().contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.when().post(REQUEST_PATH)
			.then().status(HttpStatus.OK)
			.apply(document("oauthLogin", requestPreprocessor(), responsePreprocessor(),
				requestFields(
					fieldWithPath("provider").description("소셜 로그인 서비스 제공자"),
					fieldWithPath("idToken").description("ID_TOKEN(암호화된 사용자 정보 JWT)")),
				// 응답 헤더
				responseHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("발급된 액세스 토큰 (Bearer prefix 포함)"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("발급된 리프레시 토큰 (Bearer prefix 포함)")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}

	private LoginUser buildUser(OidcUserPayload payload) {
		User user = User.builder()
			.email(payload.email())
			.provider(payload.provider())
			.socialId(payload.sub())
			.role(UserRole.USER)
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.sub())
			.build();
		return new LoginUser(user);
	}
}
