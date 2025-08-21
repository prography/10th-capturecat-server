package com.capturecat.core.api.auth;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.service.auth.TokenService;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest
class TokenApiTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenService tokenIssueService;

	private String refreshHeaderValue;

	@MockitoBean              // 실제 Redis 빈 대신 Mockito mock을 컨텍스트에 주입
	private StringRedisTemplate redisTemplate;

	private ValueOperations<String, String> valueOps;

	@BeforeEach
	void setUp() {
		// --- Redis 계층 스텁 구성 ---
		valueOps = mock(ValueOperations.class);
		given(redisTemplate.opsForValue()).willReturn(valueOps);

		// saveRefreshToken() 호출에서 쓰이는 set(...)은 no-op
		willDoNothing().given(valueOps)
			.set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

		// 블랙리스트 관련: 기본 false / 삭제는 no-op
		given(redisTemplate.hasKey(argThat(key -> key != null && key.startsWith("blacklist:")))).willReturn(false);
		willReturn(true).given(redisTemplate).delete(anyString());

		// --- 실제 서비스로 토큰 발급 (여기서 saveRefreshToken이 호출되어도 위에서 no-op 처리됨) ---
		Map<TokenType, String> issued = tokenIssueService.issue("testUser", UserRole.USER);
		String refreshToken = issued.get(TokenType.REFRESH);
		refreshHeaderValue = JwtUtil.BEARER_PREFIX + refreshToken;

		// reissue() 경로에서 parseRefreshToken()이 Redis를 조회하므로,
		// testUser의 저장된 토큰으로 동일한 값을 돌려주도록 get(...) 스텁
		// (TokenService의 키 프리픽스: "refresh_token:")
		given(valueOps.get("refresh_token:testUser")).willReturn(refreshToken);
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
