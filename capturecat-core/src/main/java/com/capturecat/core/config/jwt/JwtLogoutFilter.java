package com.capturecat.core.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

@RequiredArgsConstructor
public class JwtLogoutFilter extends GenericFilterBean {

	private static final String LOGOUT_PATH = "/logout";
	private final TokenService tokenService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {
		doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, filterChain);
	}

	/** 로그아웃
	 *  Refresh 토큰을 받아 DB에서 삭제 후 쿠키 null로 초기화하여 응답
	 *  (모든 기기에서 로그아웃 시 username 기반으로 모든 refresh 토큰 삭제)
	 */
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {

		// 로그아웃 요청일 때
		if (!request.getRequestURI().equals(LOGOUT_PATH)) {
			filterChain.doFilter(request, response);
			return;
		}

		String refreshHeader = request.getHeader(JwtUtil.REFRESH_TOKEN_HEADER);
		// 로그아웃
		try {
			if (refreshHeader == null || refreshHeader.isEmpty()) {
				throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
			}
			tokenService.deleteRefreshToken(refreshHeader);
		} catch (CoreException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(), ApiResponse.error(e.getErrorType()));
			return;
		}

		// 성공 응답
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.success());
	}
}
