package com.capturecat.core.service.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.auth.RefreshToken;
import com.capturecat.core.domain.auth.RefreshTokenRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * Access 토큰 만료시, Access/Refresh 토큰을 발행하고,
	 * 새로운 Refresh 토큰을 DB에 저장한다. (rotating)
	 */
	public Map<TokenType, String> issue(String username, String role) {
		//Access token 생성, Refresh token rotating
		String accessToken = jwtUtil.generateToken(username, role, TokenType.ACCESS);
		String refreshToken = jwtUtil.generateToken(username, role, TokenType.REFRESH);

		//Refresh token 저장
		saveRefreshToken(username, refreshToken);

		Map<TokenType, String> tokenMap = new HashMap<>();
		tokenMap.put(TokenType.ACCESS, accessToken);
		tokenMap.put(TokenType.REFRESH, refreshToken);

		return tokenMap;
	}

	public Map<TokenType, String> reissue(String authHeader) {
		String refreshToken = deleteRefreshToken(authHeader);
		//새 토큰 발급 후 Refresh 토큰 저장
		return issue(jwtUtil.getUsername(refreshToken), jwtUtil.getRole(refreshToken));
	}

	@Transactional(readOnly = true)
	public String parseRefreshToken(String authHeader) {

		if (authHeader == null || !authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
			throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
		}

		String refreshToken = authHeader.substring(JwtUtil.BEARER_PREFIX.length()).trim();
		log.info("Refresh Token: {}", refreshToken);

		try {
			if (refreshToken.isEmpty()
				|| !jwtUtil.isRefreshToken(refreshToken) //토큰 만료 검사 포함
				|| !refreshTokenRepository.existsByRefreshToken(refreshToken)) {
				throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
			}
		} catch (ExpiredJwtException e) {
			throw new CoreException(ErrorType.REFRESH_TOKEN_EXPIRED);
		} catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
			throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
		}

		return refreshToken;
	}

	public String deleteRefreshToken(String authHeader) {
		//Refresh token parsing 및 유효성 검사
		String refreshToken = parseRefreshToken(authHeader);
		//기존 Refresh 토큰 삭제
		refreshTokenRepository.deleteByRefreshToken(refreshToken);
		return refreshToken;
	}

	private void saveRefreshToken(String username, String refreshToken) {
		RefreshToken token = RefreshToken.builder()
			.username(username)
			.refreshToken(refreshToken)
			.build();

		refreshTokenRepository.save(token);
	}
}
