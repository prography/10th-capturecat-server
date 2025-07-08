package com.capturecat.core.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Getter
@AllArgsConstructor
public enum UserRole {
	ADMIN("관리자"),
	PREMIUM_USER("유료 사용자"),
	USER("일반 사용자");

	private final String value;

	public static UserRole fromRoleString(String roleName) {
		return switch (roleName) {
			case "ROLE_ADMIN" -> ADMIN;
			case "ROLE_PREMIUM_USER" -> PREMIUM_USER;
			case "ROLE_USER" -> USER;
			default -> throw new CoreException(ErrorType.UNKNOWN_ROLE);
		};
	}

	public String toRoleString() {
		return "ROLE_" + this.name();
	}
}
