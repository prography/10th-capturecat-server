package com.capturecat.core.service.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

	private final JwtUtil jwtUtil;
	private final StringRedisTemplate redisTemplate;

	@Value("${jwt.refresh-token-expiration}")
	long refreshTokenExpiration;
	private static final String BLACKLIST_PREFIX = "blacklist:";
	private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

	/**
	 * Access 토큰 만료시, Access/Refresh 토큰을 발행하고,
	 * 새로운 Refresh 토큰을 DB에 저장한다. (rotating)
	 */
	public Map<TokenType, String> issue(String username, UserRole role) {
		//Access token 생성, Refresh token rotating
		String accessToken = jwtUtil.generateToken(username, role.toRoleString(), TokenType.ACCESS);
		String refreshToken = jwtUtil.generateToken(username, role.toRoleString(), TokenType.REFRESH);

		//Refresh token 저장
		saveRefreshToken(username, refreshToken);

		Map<TokenType, String> tokenMap = new HashMap<>();
		tokenMap.put(TokenType.ACCESS, accessToken);
		tokenMap.put(TokenType.REFRESH, refreshToken);

		return tokenMap;
	}

	public Map<TokenType, String> reissue(String authHeader) {
		//기존 refresh token 삭제 (유효성 검사 포함)
		String refreshToken = deleteValidRefreshToken(authHeader);
		//새 토큰 발급 후 Refresh 토큰 저장
		return issue(jwtUtil.getUsername(refreshToken), UserRole.fromRoleString(jwtUtil.getRole(refreshToken)));
	}

	/**
	 * Refresh token이 유효한지 확인하고 파싱
	 */
	@Transactional(readOnly = true)
	public String parseRefreshToken(String authHeader) {

		if (authHeader == null || !authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
			throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
		}

		String refreshToken = authHeader.substring(JwtUtil.BEARER_PREFIX.length()).trim();
		log.info("Refresh Token: {}", refreshToken);

		try {
			if (refreshToken.isEmpty() || !jwtUtil.isRefreshToken(refreshToken)) { //토큰 만료 검사 포함
				throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
			}
			// Redis에 저장되어 있는 Refresh token 인지 확인
			String username = jwtUtil.getUsername(refreshToken);
			String savedToken = redisTemplate.opsForValue().get(getRefreshTokenKey(username));
			if (savedToken == null || !savedToken.equals(refreshToken)) {
				throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
			}
		} catch (ExpiredJwtException e) {
			throw new CoreException(ErrorType.REFRESH_TOKEN_EXPIRED);
		} catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
			throw new CoreException(ErrorType.INVALID_REFRESH_TOKEN);
		}

		return refreshToken;
	}

	/**
	 * Redis에 Refresh token 저장
	 */
	private void saveRefreshToken(String username, String refreshToken) {
		redisTemplate.opsForValue().set(
			getRefreshTokenKey(username),
			refreshToken,
			refreshTokenExpiration, TimeUnit.MILLISECONDS
		);
	}

	/**
	 * 로그아웃, 회원 탈퇴 시
	 * Refresh Token 삭제 및 Access Token 블랙리스트 등록
	 */
	public void revokeUserTokens(String accessTokenHeader, String refreshTokenHeader) {
		try {
			blacklistAccessToken(accessTokenHeader);
			deleteValidRefreshToken(refreshTokenHeader);
		} catch (Exception e) {
			throw new CoreException(ErrorType.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 유효성 검사 후 Redis에서 Refresh token 삭제
	 */
	public String deleteValidRefreshToken(String refreshTokenHeader) {
		//Refresh token parsing 및 유효성 검사
		String refreshToken = parseRefreshToken(refreshTokenHeader);
		String username = jwtUtil.getUsername(refreshToken);
		log.debug("Deleting refresh token for user {}", username);
		//기존 Refresh 토큰 삭제
		redisTemplate.delete(getRefreshTokenKey(username));
		return refreshToken;
	}

	public void deleteRefreshTokenByUsername(String username) {
		redisTemplate.delete(getRefreshTokenKey(username));
	}

	private String getRefreshTokenKey(String username) {
		return REFRESH_TOKEN_PREFIX + username;
	}

	/**
	 * header 에서 Access Token 파싱 후 블랙리스팅
	 */
	public void blacklistAccessToken(String authHeader) {
		String accessToken = jwtUtil.resolveToken(authHeader);
		long remainMillis = jwtUtil.getExpiration(accessToken) - System.currentTimeMillis();

		if (remainMillis > 0) {
			redisTemplate.opsForValue()
				.set(blacklistKey(accessToken), "blacklisted", remainMillis, TimeUnit.MILLISECONDS);
		}
		log.info("Blacklist Token: {}", accessToken);
	}

	public boolean isBlacklisted(String accessToken) {
		return redisTemplate.hasKey(blacklistKey(accessToken));
	}

	private String blacklistKey(String token) {
		return BLACKLIST_PREFIX + token;
	}
}
