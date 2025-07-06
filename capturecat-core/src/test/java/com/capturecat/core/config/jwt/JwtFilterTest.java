package com.capturecat.core.config.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

import com.capturecat.core.service.auth.LoginUser;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

	@Mock
	private JwtUtil jwtUtil;
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
		request.addHeader("Authorization", "Bearer expired.token");
		doThrow(new ExpiredJwtException(null, null, "토큰 만료")).when(jwtUtil).isExpired("expired.token");

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@Test
	void 서명이_잘못된_토큰이면_401응답() throws Exception {
		// given
		request.addHeader("Authorization", "Bearer invalid.token");
		doThrow(new SignatureException("잘못된 서명")).when(jwtUtil).isExpired("invalid.token");

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
	}

	@Test
	void 정상_토큰이면_SecurityContext에_등록() throws Exception {
		// given
		request.addHeader("Authorization", "Bearer valid.token");

		when(jwtUtil.isExpired("valid.token")).thenReturn(false);
		when(jwtUtil.isAccessToken("valid.token")).thenReturn(true);
		when(jwtUtil.getUsername("valid.token")).thenReturn("user1");
		when(jwtUtil.getRole("valid.token")).thenReturn("ROLE_USER");

		// when
		jwtFilter.doFilterInternal(request, response, filterChain);

		// then
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(authentication);
		assertEquals("user1", ((LoginUser)authentication.getPrincipal()).getUsername());
		verify(filterChain).doFilter(request, response);
	}
}
