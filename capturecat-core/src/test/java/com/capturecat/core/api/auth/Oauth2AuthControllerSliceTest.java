package com.capturecat.core.api.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.capturecat.core.api.CustomWebMvcTest;
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
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@CustomWebMvcTest(Oauth2AuthController.class)
class Oauth2AuthControllerSliceTest {

	private static final String REQUEST_PATH = "/v1/auth/{provider}/login";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SocialService socialService;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private TokenService tokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("구글, 카카오 소셜 로그인 성공 - JWT 토큰 헤더, 응답 OK")
	@Test
	void socialLogin_success() throws Exception {
		// given
		String provider = "google";
		String idToken = "test-id-token";
		SocialLoginRequest req = new SocialLoginRequest(idToken, null, null, false);
		OidcUserPayload payload =
			new OidcUserPayload(provider, "1234", "test@test.com", "testNickname", null, true);

		LoginUser user = buildUser(payload);

		Map<TokenType, String> tokenMap = Map.of(
			TokenType.ACCESS, "access.jwt.token",
			TokenType.REFRESH, "refresh.jwt.token"
		);

		// idTokenVerifierService.verifyAndExtract → payload
		Mockito.when(socialService.verifyAndExtract(anyString(), any(), any(), any()))
			.thenReturn(payload);
		// userService.upsertSocialUser → user
		Mockito.when(userService.upsertSocialUser(payload, false)).thenReturn(user);
		// tokenService.issue → tokenMap
		Mockito.when(tokenService.issue(eq(user.getUsername()), eq(user.getRole())))
			.thenReturn(tokenMap);

		// when & then
		mockMvc.perform(post(REQUEST_PATH, provider)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + "access.jwt.token"))
			.andExpect(header().string(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + "refresh.jwt.token"))
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").exists());
	}

	@DisplayName("애플 소셜 로그인 성공 - idToken이 아닌 authorization_code 송신")
	@Test
	void socialLogin_apple_success() throws Exception {
		// given
		String provider = "apple";
		String authorization_code = "test_authorization_code";
		String nickname = "최재량";
		SocialLoginRequest req = new SocialLoginRequest(null, nickname, authorization_code, false);
		OidcUserPayload payload =
			new OidcUserPayload(provider, "1234", "test@test.com", nickname, "apple_refresh_token", true);

		LoginUser user = buildUser(payload);

		Map<TokenType, String> tokenMap = Map.of(
			TokenType.ACCESS, "access.jwt.token",
			TokenType.REFRESH, "refresh.jwt.token"
		);

		// idTokenVerifierService.verifyAndExtract → payload
		Mockito.when(socialService.verifyAndExtract(anyString(), any(), any(), any()))
			.thenReturn(payload);
		// userService.upsertSocialUser → user
		Mockito.when(userService.upsertSocialUser(payload, false)).thenReturn(user);
		// tokenService.issue → tokenMap
		Mockito.when(tokenService.issue(eq(user.getUsername()), eq(user.getRole())))
			.thenReturn(tokenMap);

		// when & then
		mockMvc.perform(post(REQUEST_PATH, provider)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + "access.jwt.token"))
			.andExpect(header().string(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + "refresh.jwt.token"))
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").exists());
	}

	@DisplayName("id_token 검증 실패 시 401 응답")
	@Test
	void socialLogin_invalidIdToken() throws Exception {
		// given
		String provider = "google";
		String idToken = "bad-id-token";
		String nickname = null;
		SocialLoginRequest req = new SocialLoginRequest(idToken, nickname, null, false);

		Mockito.when(socialService.verifyAndExtract(any(), any(), any(), any()))
			.thenThrow(new CoreException(ErrorType.INVALID_ID_TOKEN));

		// when & then
		mockMvc.perform(post(REQUEST_PATH, provider)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isUnauthorized()) // 실제 예외 핸들러에 따라 다를 수 있음
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.error").exists());
	}

	private LoginUser buildUser(SocialService.OidcUserPayload payload) {
		User user = User.builder()
			.email(payload.email())
			.role(UserRole.USER)
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.socialId())
			.nickname(payload.nickname())
			.build();
		return new LoginUser(user);
	}

}
