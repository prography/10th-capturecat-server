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

@Component
public class JwtUtil {

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

	// 만료 여부 검증 (예외 던짐)
	public boolean isExpired(String token) {
		extractClaims(token); // parseSignedClaims 호출 시 자동 만료 검증
		return false;
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

	public boolean isAccessToken(String accessToken) {
		return getTokenType(accessToken).equals(TokenType.ACCESS.name());
	}
}
