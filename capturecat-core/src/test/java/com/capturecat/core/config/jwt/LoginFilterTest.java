package com.capturecat.core.config.jwt;

import static com.capturecat.core.DummyObject.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.capturecat.core.api.user.dto.UserReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.LoginReqDto;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.support.error.ErrorCode;
import com.capturecat.core.support.error.ErrorType;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class LoginFilterTest {
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.save(newUser("testUser"));
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
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
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
