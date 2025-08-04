package com.capturecat.core.api.user;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;

@WebMvcTest(UserController.class)
public class UserApiMockTest {
	public static final String URL_PREFIX = "/v1/user";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean
	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		LoginUser loginUser = new LoginUser("testUser", "ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities())
		);
		// JwtFilter를 통과시키는 stub
		given(jwtUtil.resolveToken(anyString())).willReturn("valid.access.token");
		given(jwtUtil.isAccessToken(anyString())).willReturn(true);
		given(jwtUtil.isValid(anyString())).willReturn(true);
		given(jwtUtil.getUsername(anyString())).willReturn("testUser");
		given(jwtUtil.getRole(anyString())).willReturn("ROLE_USER");
		given(tokenService.isBlacklisted(anyString())).willReturn(false);
	}

	@Test
	@WithMockUser(username = "testUser", roles = "USER")
	@DisplayName("회원 탈퇴 시 userService, tokenService가 정상 호출되고 성공 응답을 반환한다")
	void withdraw_IntegrationTest() throws Exception {
		// given
		String accessToken = "Bearer test.access.token";
		String resultMessage = "소셜 서비스 해지 요청 결과";

		// userService가 호출되면 반환할 메시지
		given(userService.withdraw(Mockito.any(LoginUser.class))).willReturn(resultMessage);

		// when & then
		mockMvc.perform(delete(URL_PREFIX + "/withdraw")
				.with(csrf())
				.header(HttpHeaders.AUTHORIZATION, accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result").value("SUCCESS"))
			.andExpect(jsonPath("$.data").value(resultMessage));

		// 내부 서비스 호출 검증 (MockBean으로 대체했으므로)
		then(userService).should().withdraw(Mockito.any(LoginUser.class));
		then(tokenService).should().deleteRefreshTokenByUsername("testUser");
		then(tokenService).should().blacklistAccessToken(accessToken);
	}
}
