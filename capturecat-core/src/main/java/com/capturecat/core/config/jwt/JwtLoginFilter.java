package com.capturecat.core.config.jwt;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.api.user.dto.UserReqDto.LoginReqDto;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

/**
 * 소셜 로그인/회원가입이 아닌,
 * 일반 회원가입 후 /login 경로로, id, password로 로그인한 경우 (개발 용도)
 */
@Slf4j
@RequiredArgsConstructor
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	private final TokenService tokenIssueService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		try {
			LoginReqDto loginReqDto = objectMapper.readValue(request.getInputStream(), LoginReqDto.class);

			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword());

			return authenticationManager.authenticate(authToken);
		} catch (IOException e) {
			throw new CoreException(ErrorType.LOGIN_FAIL);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) throws IOException {
		//JWT 발급
		LoginUser loginUser = (LoginUser)authResult.getPrincipal();
		String username = loginUser.getUsername();
		String role = loginUser.getAuthorities().iterator().next().getAuthority();

		//토큰 발급
		Map<TokenType, String> tokenMap = tokenIssueService.issue(username, UserRole.fromRoleString(role));
		log.info("[JwtLoginFilter.successfulAuthentication] 사용자 로그인({}), 토큰 발급", username);

		//Header에 실어 응답
		response.setHeader(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + tokenMap.get(TokenType.ACCESS));
		response.setHeader(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + tokenMap.get(TokenType.REFRESH));
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.success());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorType.LOGIN_FAIL));
	}

}
