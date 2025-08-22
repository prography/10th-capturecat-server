package com.capturecat.core.api.auth;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

import com.capturecat.core.config.jwt.JwtLogoutFilter;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.test.api.RestDocsTest;

class LogInOutTest extends RestDocsTest {

	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		tokenService = mock(TokenService.class);
		MockMvc mvc = MockMvcBuilders.standaloneSetup()
			.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
			.addFilters(new JwtLogoutFilter(tokenService))
			.build();
		mockMvc = RestAssuredMockMvc.given().mockMvc(mvc);
	}

	@Test
	void 로그아웃() {
		//given
		String accessTokenHeader = "valid-access-token-header";
		String refreshTokenHeader = "valid-refresh-token-header";
		willDoNothing().given(tokenService).revokeUserTokens(anyString(), anyString());

		//when & then
		given()
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + accessTokenHeader)
			.header(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + refreshTokenHeader)
			.when().post("/logout")
			.then().statusCode(HttpStatus.SC_OK)
			.apply(document("logout", requestPreprocessor(), responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("엑세스 토큰 (Bearer prefix 포함)"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("리프레시 토큰 (Bearer prefix 포함)")),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과"))));
	}
}
