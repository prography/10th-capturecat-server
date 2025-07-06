package com.capturecat.core.api.auth.dto;

public record OauthLoginRequest(
	String provider,
	String idToken
) {
}
