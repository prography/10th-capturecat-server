package com.capturecat.core.api.auth.dto;

public record SocialLoginResponse(String email, String nickname, boolean tutorialCompleted) {
}
