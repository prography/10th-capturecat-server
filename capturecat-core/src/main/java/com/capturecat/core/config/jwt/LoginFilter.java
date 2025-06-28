package com.capturecat.core.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.LoginReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.LoginRespDto;
import com.capturecat.core.config.auth.LoginUser;
import com.capturecat.core.domain.user.RefreshToken;
import com.capturecat.core.domain.user.RefreshTokenRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.ApiResponse;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
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
		Authentication authResult) throws IOException, ServletException {
		//JWT 발급
		LoginUser loginUser = (LoginUser)authResult.getPrincipal();
		String username = loginUser.getUsername();
		String authority = loginUser.getAuthorities().iterator().next().getAuthority();

		String accessToken = jwtUtil.generateToken(username, authority, TokenType.ACCESS);
		String refreshToken = jwtUtil.generateToken(username, authority, TokenType.REFRESH);

		//Refresh token 저장
		saveRefreshToken(username, refreshToken);

		//발급 토큰 응답
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.success(new LoginRespDto(accessToken, refreshToken)));
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorType.LOGIN_FAIL));
	}

	private void saveRefreshToken(String username, String refreshToken) {
		RefreshToken token = RefreshToken.builder()
			.username(username)
			.refreshToken(refreshToken)
			.build();

		refreshTokenRepository.save(token);
	}
}
