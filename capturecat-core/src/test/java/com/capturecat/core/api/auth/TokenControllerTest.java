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
import org.springframework.restdocs.payload.JsonFieldType;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.test.api.RestDocsTest;

public class TokenControllerTest extends RestDocsTest {

	private TokenController tokenController;

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		tokenService = mock(TokenService.class);
		tokenController = new TokenController(tokenService);
		mockMvc = mockController(tokenController);
	}

	@Test
	void 엑세스토큰_만료시_재발행() {
		// given
		String oldRefresh = "old-refresh-token";
		String newAccess = "new-access-token";
		String newRefresh = "new-refresh-token";
		Map<TokenType, String> newTokens = Map.of(
			TokenType.ACCESS, newAccess,
			TokenType.REFRESH, newRefresh
		);
		willReturn(oldRefresh).given(tokenService).parseRefreshToken(anyString());
		willReturn(newTokens).given(tokenService).reissue(anyString());

		// when & then
		given().header(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + oldRefresh)
			.when().post("/token/reissue")
			.then().status(HttpStatus.OK)
			.apply(document("reissue", requestPreprocessor(), responsePreprocessor(),
				// 요청 헤더
				requestHeaders(
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("재발급에 사용될 리프레시 토큰 (Bearer prefix 포함)")),
				// 응답 헤더
				responseHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("재발급된 액세스 토큰 (Bearer prefix 포함)"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("재발급된 리프레시 토큰 (Bearer prefix 포함)")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"),
					fieldWithPath("data").type(JsonFieldType.NULL).ignored(),
					fieldWithPath("error").type(JsonFieldType.OBJECT).ignored())));
	}
}
