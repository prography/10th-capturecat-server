package com.capturecat.core.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.service.auth.TokenIssueService;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class TokenIssueApiTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenIssueService tokenIssueService;

	private String refreshHeaderValue;

	@BeforeEach
	void setUp() {
		// 실제 서비스로 토큰 발급 (DB에 RefreshToken도 저장됨)
		Map<TokenType, String> issued = tokenIssueService.issue("testuser", "ROLE_USER");
		String refreshToken = issued.get(TokenType.REFRESH);
		refreshHeaderValue = JwtUtil.BEARER_PREFIX + refreshToken;
	}

	@Test
	@DisplayName("Refresh token 재발급 성공")
	void reissue_withValidRefreshToken_returnsNewTokensInHeaders() throws Exception {
		mockMvc.perform(post("/token/reissue")
				.header(JwtUtil.REFRESH_TOKEN_HEADER, refreshHeaderValue))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(header().exists(HttpHeaders.AUTHORIZATION))
			.andExpect(header().exists(JwtUtil.REFRESH_TOKEN_HEADER))
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	@DisplayName("Refresh token 재발급 실패 - 잘못된 Refresh token으로 요청할 경우 에러 응답")
	void reissue_withInvalidRefreshToken_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/token/reissue")
				.header(JwtUtil.REFRESH_TOKEN_HEADER, "Bearer invalid.token"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.error").exists());
	}
}
