package com.capturecat.core.api.auth.dto;

public record SocialLoginRequest(String idToken, String nickname, String authToken, boolean accountLinking) {
}
