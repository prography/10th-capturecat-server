package com.capturecat.core.config.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
public class JwtUtil {

	public static final String BEARER_PREFIX = "Bearer ";
	public static final String REFRESH_TOKEN_HEADER = "Refresh-Token";

	@Value("${jwt.secret}")
	private String secretKeyPlain;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		secretKey = new SecretKeySpec(secretKeyPlain.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	/** 토큰 생성 */
	public String generateToken(String username, String role, TokenType type) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + getExpirationForType(type));

		return Jwts.builder()
			.subject(username)
			.claim("role", role)
			.claim("type", type.name())
			.issuedAt(now)
			.expiration(expiry)
			.signWith(secretKey)
			.compact();
	}

	/** 검증 */
	// 공통 Claims 추출
	private Claims extractClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public String resolveToken(String authHeader) {
		if (authHeader == null || !authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
			throw new CoreException(ErrorType.INVALID_ACCESS_TOKEN);
		}
		return authHeader.substring(BEARER_PREFIX.length()).trim();
	}

	// JWT 파싱하여 만료일자(ms) 반환
	public long getExpiration(String token) {
		Claims claims = extractClaims(token);
		return claims.getExpiration().getTime();
	}

	// username(subject) 추출
	public String getUsername(String token) {
		return extractClaims(token).getSubject();
	}

	// role 추출
	public String getRole(String token) {
		return extractClaims(token).get("role", String.class);
	}

	//type 추출
	public String getTokenType(String token) {
		return extractClaims(token).get("type", String.class);
	}

	private long getExpirationForType(TokenType type) {
		return (type == TokenType.ACCESS) ? accessTokenExpiration : refreshTokenExpiration;
	}

	public boolean isInValidToken(String token, TokenType tokenType) {
		try {
			return !getTokenType(token).equals(tokenType.name());
		} catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
			return true;
		}
	}
}
