package com.capturecat.core.config.jwt;

import java.io.IOException;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

@Slf4j
@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {

	private static final String LOGOUT_PATH = "/logout";
	private final TokenService tokenService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// 1) ERROR/ASYNC 등 요청 아닌 디스패치 스킵
		if (request.getDispatcherType() != DispatcherType.REQUEST) {
			return true;
		}
		// 2) POST만 허용
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		// 3) /logout 경로만 통과 (컨텍스트 경로 영향 없는 servletPath 사용)
		return !LOGOUT_PATH.equals(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws IOException {
		try {
			String accessHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			String refreshHeader = request.getHeader(JwtUtil.REFRESH_TOKEN_HEADER);
			if (!StringUtils.hasText(accessHeader) || !StringUtils.hasText(refreshHeader)) {
				throw new CoreException(ErrorType.INVALID_LOGOUT_AUTH_TOKEN);
			}
			log.info("LogoutFilter hit: dispatcher={}, path={}", request.getDispatcherType(), request.getServletPath());

			tokenService.revokeUserTokens(accessHeader, refreshHeader);

			response.setStatus(HttpStatus.OK.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(), ApiResponse.success());
		} catch (CoreException e) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(), ApiResponse.error(e.getErrorType()));
		} catch (Exception e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			objectMapper.writeValue(response.getWriter(),
				ApiResponse.error(ErrorType.INTERNAL_SERVER_ERROR));
		}
		// 체인 종료 (추가 필터로 넘기지 않음)
	}
}
