package com.capturecat.core.api.user;

import static com.capturecat.core.DummyObject.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.support.error.ErrorCode;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void 회원가입_정상() throws Exception {
		//given
		JoinReqDto joinReqDto = new JoinReqDto();
		joinReqDto.setUsername("username1");
		joinReqDto.setPassword("password1");
		joinReqDto.setEmail("username1@email.com");

		String requestBody = objectMapper.writeValueAsString(joinReqDto);

		//when & then
		mockMvc.perform(post("/v1/user/join")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data.username").value("username1@email.com"))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	public void 회원가입_실패_중복회원() throws Exception {
		//given
		String dupUsername = "testUser";
		userRepository.save(newUser(dupUsername));

		JoinReqDto reqDto = new JoinReqDto();
		reqDto.setUsername(dupUsername);
		reqDto.setPassword("password");
		reqDto.setEmail("test@email.com");

		String requestBody = objectMapper.writeValueAsString(reqDto);

		//when & then
		mockMvc.perform(post("/v1/user/join")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.result").value("ERROR"))
			.andExpect(jsonPath("$.error.code").value(ErrorCode.ALREADY_EXISTS_USERNAME.name()))
			.andExpect(jsonPath("$.error.message").value(ErrorCode.ALREADY_EXISTS_USERNAME.getMessage()));
	}

	@Test
	public void 회원가입_실패_유효성검사() throws Exception {
		//given
		JoinReqDto reqDto = new JoinReqDto();
		reqDto.setUsername("username");

		String requestBody = objectMapper.writeValueAsString(reqDto);

		//when & then
		mockMvc.perform(post("/v1/user/join")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value(ErrorCode.BEAN_VALIDATION_FAIL.name()))
			.andExpect(jsonPath("$.error.message").value(ErrorCode.BEAN_VALIDATION_FAIL.getMessage()));
	}
}
