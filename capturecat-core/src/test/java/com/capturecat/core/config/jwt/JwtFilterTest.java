package com.capturecat.core.config.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private TokenService tokenService;
	@Mock
	private FilterChain filterChain;
	@InjectMocks
	private JwtFilter jwtFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void init() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		SecurityContextHolder.clearContext();
	}

	@Test
	void Authorization_헤더가_없으면_필터_통과() throws Exception {
		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void 만료된_토큰이면_401응답() throws Exception {
		// given
		String token = "expired.token";
		request.addHeader(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + token);
		when(jwtUtil.resolveToken(JwtUtil.BEARER_PREFIX + token)).thenReturn(token);
		when(jwtUtil.isAccessToken(token)).thenReturn(true);
		when(jwtUtil.isValid(token)).thenReturn(false);

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@Test
	void 서명이_잘못된_토큰이면_401응답() throws Exception {
		// given
		String token = "invalid.token";
		request.addHeader(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + token);
		when(jwtUtil.resolveToken(JwtUtil.BEARER_PREFIX + token)).thenReturn(token);
		when(jwtUtil.isAccessToken(token)).thenReturn(true);
		when(jwtUtil.isValid(token)).thenReturn(false);

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@Test
	void 정상_토큰이면_SecurityContext에_등록() throws Exception {
		// given
		String token = "valid.token";
		request.addHeader(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + token);

		when(jwtUtil.resolveToken(JwtUtil.BEARER_PREFIX + token)).thenReturn(token);
		when(jwtUtil.isAccessToken(token)).thenReturn(true);
		when(jwtUtil.isValid(token)).thenReturn(true);
		when(tokenService.isBlacklisted(token)).thenReturn(false);
		when(jwtUtil.getUsername(token)).thenReturn("user1");
		when(jwtUtil.getRole(token)).thenReturn("ROLE_USER");

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(authentication);
		assertEquals("user1", ((LoginUser)authentication.getPrincipal()).getUsername());
		verify(filterChain).doFilter(request, response);
	}
}
