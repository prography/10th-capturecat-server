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
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@CustomWebMvcTest(Oauth2AuthController.class)
class Oauth2AuthControllerSliceTest {

	private static final String REQUEST_PATH = "/v1/auth/oauth2/login";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IdTokenVerifierService idTokenVerifierService;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private TokenService tokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("소셜 로그인 성공 - JWT 토큰 헤더, 응답 OK")
	@Test
	void oauthLogin_success() throws Exception {
		// given
		String provider = "google";
		String idToken = "test-id-token";
		OauthLoginRequest req = new OauthLoginRequest(provider, idToken);
		OidcUserPayload payload =
			new OidcUserPayload(provider, "1234", "test@test.com", true);

		LoginUser user = buildUser(payload);

		Map<TokenType, String> tokenMap = Map.of(
			TokenType.ACCESS, "access.jwt.token",
			TokenType.REFRESH, "refresh.jwt.token"
		);

		// idTokenVerifierService.verifyAndExtract → payload
		Mockito.when(idTokenVerifierService.verifyAndExtract(eq(provider), eq(idToken)))
			.thenReturn(payload);
		// userService.upsertSocialUser → user
		Mockito.when(userService.upsertSocialUser(payload)).thenReturn(user);
		// tokenService.issue → tokenMap
		Mockito.when(tokenService.issue(eq(user.getUsername()), eq(user.getRole())))
			.thenReturn(tokenMap);

		// when & then
		mockMvc.perform(post(REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + "access.jwt.token"))
			.andExpect(header().string(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + "refresh.jwt.token"))
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@DisplayName("id_token 검증 실패 시 401 응답")
	@Test
	void oauthLogin_invalidIdToken() throws Exception {
		// given
		String provider = "google";
		String idToken = "bad-id-token";
		OauthLoginRequest req = new OauthLoginRequest(provider, idToken);

		Mockito.when(idTokenVerifierService.verifyAndExtract(eq(provider), eq(idToken)))
			.thenThrow(new CoreException(ErrorType.INVALID_ID_TOKEN));

		// when & then
		mockMvc.perform(post(REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))) //컨트롤러 슬라이스 테스트 시 security config가 load 되지 않아 추가)
			.andExpect(status().isUnauthorized()) // 실제 예외 핸들러에 따라 다를 수 있음
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.error").exists());
	}

	private LoginUser buildUser(IdTokenVerifierService.OidcUserPayload payload) {
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
