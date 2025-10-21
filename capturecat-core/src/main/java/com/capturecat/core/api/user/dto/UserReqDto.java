package com.capturecat.core.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.domain.user.UserSettings;

/** 회원 관련 요청 DTO */
public class UserReqDto {

	/** 회원 가입 요청 DTO */
	@Getter
	@Setter
	public static class JoinReqDto {
		// @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요")
		// TODO: 우선 이메일로 회원이름을 정한다. 닉네임을 받을지는 추후 논의
		@NotEmpty
		private String username;

		@NotEmpty
		@Size(min = 8, max = 20)
		private String password;

		@NotEmpty
		@Email
		private String email;

		public User toEntity(PasswordEncoder passwordEncoder) {
			return User.builder()
				.username(email)
				.password(passwordEncoder.encode(password))
				.email(email)
				.role(UserRole.USER)
				.nickname(username)
				.build();
		}
	}

	/** 로그인 요청 DTO */
	@Getter
	@Setter
	public static class LoginReqDto {
		private String username;
		private String password;
	}

	/** 탈퇴 사유 DTO */
	@Getter
	@Setter
	public static class WithdrawReqDto {
		@Size(max = 500, message = "탈퇴 사유는 500자 이내로 작성해주세요.")
		private String reason;
	}

	/** 회원 설정 정보 DTO */
	@Getter
	@Setter
	public static class UserSettingsReqDto {
		private boolean screenshotAutoDeleteEnabled;
	}
}
