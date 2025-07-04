package com.capturecat.core.config.jwt;

import static com.capturecat.core.config.jwt.JwtUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import jakarta.servlet.FilterChain;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
	@DisplayName("/logout 요청이 아닐 경우 무시")
	void non_logout_request() throws Exception {
		//given
		request.setRequestURI("/not-logout");

		//when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		//then
		verify(filterChain).doFilter(request, response);
	}

	@Test
	@DisplayName("리프레시 토큰이 없는 경우 UNAUTHORIZED 응답")
	void logout_request_without_token() throws Exception {
		// given
		request.setRequestURI("/logout");

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
		verify(tokenService, never()).deleteRefreshToken(anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("토큰 검증 도중 예외 발생 시 INVALID_REFRESH_TOKEN 에러 발생")
	void logout_request_blacklist_exception() throws Exception {
		// given
		request.setRequestURI("/logout");
		String refreshToken = "invalid refresh-token";
		request.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + refreshToken);

		doThrow(new CoreException(ErrorType.INVALID_REFRESH_TOKEN)).when(tokenService).deleteRefreshToken(anyString());

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
		verify(tokenService).deleteRefreshToken(anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("유효한 리프레시 토큰이 있는 경우 정상 처리")
	void logout_request_with_valid_token() throws Exception {
		// given
		request.setRequestURI("/logout");
		String refreshToken = "valid-refresh-token";
		request.addHeader(REFRESH_TOKEN_HEADER, BEARER_PREFIX + refreshToken);

		// 토큰 서비스 mock: 항상 정상 처리
		when(tokenService.deleteRefreshToken(anyString())).thenReturn(refreshToken);

		// when
		jwtLogoutFilter.doFilter(request, response, filterChain);

		// then
		assertEquals(HttpStatus.SC_OK, response.getStatus());
		verify(tokenService).deleteRefreshToken(anyString());
		verify(filterChain, never()).doFilter(any(), any());
	}
}
