package com.capturecat.core.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
	ADMIN("관리자"), PREMIUM_USER("유료 사용자"), USER("일반 사용자");

	private final String value;
}
