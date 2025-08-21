package com.capturecat.core.config.jwt;

import static com.capturecat.core.DummyObject.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.capturecat.core.api.user.dto.UserReqDto.LoginReqDto;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.support.error.ErrorCode;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class JwtLoginFilterTest {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@MockitoBean              // 실제 Redis 빈 대신 Mockito mock을 컨텍스트에 주입
	private StringRedisTemplate redisTemplate;

	private ValueOperations<String, String> valueOps;

	@BeforeEach
	void setUp() {
		userRepository.save(newUser("testUser"));

		// --- Redis 계층 스텁 ---
		valueOps = mock(ValueOperations.class);

		// opsForValue() 체인
		given(redisTemplate.opsForValue()).willReturn(valueOps);

		// saveRefreshToken() 경로: set(...) 은 no-op
		willDoNothing().given(valueOps).set(
			anyString(), anyString(), anyLong(), ArgumentMatchers.any(TimeUnit.class)
		);

		// 블랙리스트 관련: hasKey(..) 기본 false, delete(..) no-op
		given(redisTemplate.hasKey(argThat(key -> key != null && key.startsWith("blacklist:")))).willReturn(false);
		willReturn(true).given(redisTemplate).delete(anyString());
	}

	@Test
	void 로그인_성공() throws Exception {
		LoginReqDto loginReqDto = new LoginReqDto();
		loginReqDto.setUsername("testUser");
		loginReqDto.setPassword("password");
		String requestBody = objectMapper.writeValueAsString(loginReqDto);

		//when
		mockMvc.perform(post("/login")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			// Authorization 헤더 검사
			.andExpect(header().exists(HttpHeaders.AUTHORIZATION))
			.andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
			// Refresh-Token 헤더 검사
			.andExpect(header().exists("Refresh-Token"))
			.andExpect(header().string("Refresh-Token", startsWith("Bearer ")))
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void 로그인_실패() throws Exception {
		LoginReqDto loginReqDto = new LoginReqDto();
		loginReqDto.setUsername("testUser");
		loginReqDto.setPassword("wrongPassword");
		String requestBody = objectMapper.writeValueAsString(loginReqDto);

		//when
		mockMvc.perform(post("/login")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.data").doesNotExist())
			.andExpect(jsonPath("$.error.code").value(ErrorCode.INVALID_REQUEST.name()));
	}
}
