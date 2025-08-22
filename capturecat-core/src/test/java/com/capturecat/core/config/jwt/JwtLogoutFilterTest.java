package com.capturecat.core.config.jwt;

import static com.capturecat.core.config.jwt.JwtUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class JwtLogoutFilterTest {

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private JwtLogoutFilter jwtLogoutFilter;

	@Mock
	private FilterChain filterChain;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void init() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	@DisplayName("/logout 요청이 아니면 체인을 그대로 통과")
	void non_logout_request() throws Exception {
		// given
		request.setMethod("GET");
		request.setRequestURI("/not-logout");

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		verify(tokenService, never()).revokeUserTokens(anyString(), anyString());
	}

	@Test
	@DisplayName("POST /logout 이지만 토큰 헤더가 없으면 401")
	void logout_request_without_token() throws Exception {
		// given
		request.setMethod("POST");
		request.setRequestURI("/logout");

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
		verify(tokenService, never()).revokeUserTokens(anyString(), anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("POST /logout 중 revokeUserTokens 에서 예외 발생 시 401")
	void logout_request_blacklist_exception() throws Exception {
		// given
		request.setMethod("POST");
		request.setRequestURI("/logout");
		String accessToken = "invalid-access-token";
		String refreshToken = "invalid-refresh-token";
		request.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);
		request.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + refreshToken);

		doThrow(new CoreException(ErrorType.INVALID_REFRESH_TOKEN))
			.when(tokenService).revokeUserTokens(anyString(), anyString());

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
		verify(tokenService).revokeUserTokens(anyString(), anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("POST /logout + 유효한 토큰 헤더면 200")
	void logout_request_with_valid_token() throws Exception {
		// given
		request.setMethod("POST");
		request.setRequestURI("/logout");
		String accessToken = "valid-access-token";
		String refreshToken = "valid-refresh-token";
		request.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken);
		request.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + refreshToken);

		willDoNothing().given(tokenService).revokeUserTokens(anyString(), anyString());

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		verify(tokenService).revokeUserTokens(anyString(), anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}
}
