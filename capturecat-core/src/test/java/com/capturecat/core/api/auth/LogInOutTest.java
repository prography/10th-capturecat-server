package com.capturecat.core.api.auth;

import static com.capturecat.test.api.RestDocsUtil.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
	@DisplayName("POST /logout - 헤더에 토큰이 있으면 200 OK")
	void 로그아웃() {
		// given
		String accessTokenHeader = "valid-access-token-header";
		String refreshTokenHeader = "valid-refresh-token-header";
		willDoNothing().given(tokenService).revokeUserTokens(anyString(), anyString());

		// when & then
		given()
			.contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
			.accept(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
			.header(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + accessTokenHeader)
			.header(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + refreshTokenHeader)
			.when()
			.post("/logout")
			.then()
			.statusCode(org.apache.http.HttpStatus.SC_OK)
			// ApiResponse.success()의 스키마에 맞춰 본문 필드 검증 (필요 시 조정)
			.body("result", org.hamcrest.Matchers.equalTo("SUCCESS"))
			.apply(document("logout",
				requestPreprocessor(),
				responsePreprocessor(),
				requestHeaders(
					headerWithName(HttpHeaders.AUTHORIZATION)
						.description("엑세스 토큰 (Bearer prefix 포함)"),
					headerWithName(JwtUtil.REFRESH_TOKEN_HEADER)
						.description("리프레시 토큰 (Bearer prefix 포함)")
				),
				responseFields(
					fieldWithPath("result").type(JsonFieldType.STRING).description("요청 결과")
				)
			));

		// revokeUserTokens가 단 한 번 호출되었는지 확인
		then(tokenService).should(org.mockito.Mockito.times(1))
			.revokeUserTokens(anyString(), anyString());
	}

}
