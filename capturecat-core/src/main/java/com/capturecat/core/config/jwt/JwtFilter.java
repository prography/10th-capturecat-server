package com.capturecat.core.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.config.auth.LoginUser;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		// Authorization 헤더에서 "Bearer <token>" 추출
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
			log.info("Authorization header missing or malformed");
			filterChain.doFilter(request, response); // 다음 필터로 넘김 (비인증 요청 허용할 수 있음)
			return;
		}

		String accessToken = authHeader.substring(JwtUtil.BEARER_PREFIX.length()); // "Bearer " 이후 토큰

		//토큰 만료 검증. 만료 시 client에 즉시 응답. client는 재발급 요청 수행.
		try {
			jwtUtil.isExpired(accessToken);
		} catch (ExpiredJwtException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
			rejectInvalidToken(response, ErrorType.ACCESS_TOKEN_EXPIRED);

		}

		// 토큰 유형 검사
		if (!jwtUtil.isAccessToken(accessToken)) {
			rejectInvalidToken(response, ErrorType.INVALID_ACCESS_TOKEN);
			return;
		}

		//토큰에서 사용자 정보 추출
		String username = jwtUtil.getUsername(accessToken);
		String role = jwtUtil.getRole(accessToken);
		LoginUser loginUser = new LoginUser(username, role);
		Authentication authToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

		// SecurityContext에 등록
		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);
	}

	private void rejectInvalidToken(HttpServletResponse response, ErrorType errorType) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(errorType));
	}

}
