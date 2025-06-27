package com.capturecat.core.domain.user;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.springframework.beans.factory.annotation.Value;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

	@Id
	@GeneratedValue
	private Long id;

	private String username;
	private String refreshToken;
	private String expiration;
	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	@Builder
	public RefreshToken(String username, String refreshToken) {
		this.username = username;
		this.refreshToken = refreshToken;
		this.expiration = new Date(System.currentTimeMillis() + refreshTokenExpiration).toString();
	}
}
