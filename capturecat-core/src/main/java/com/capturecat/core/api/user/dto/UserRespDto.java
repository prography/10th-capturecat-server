package com.capturecat.core.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.capturecat.core.domain.user.User;

/** 회원 관련 응답 DTO */
public class UserRespDto {

	/** 회원 정보 응답 DTO */
	@Getter
	@Setter
	public static class InfoRespDto {
		private String email;
		private String nickname;
		private boolean tutorialCompleted;

		public InfoRespDto(User user) {
			this.email = user.getUsername();
			this.nickname = user.getNickname();
			this.tutorialCompleted = user.isTutorialCompleted();
		}
	}

	/** 로그인 응답 DTO */
	@Getter
	@Setter
	@AllArgsConstructor
	public static class LoginRespDto {
		private String accessToken;
		private String refreshToken;
	}

	/** 회원 가입 응답 DTO */
	@Getter
	@Setter
	public static class JoinRespDto {
		private Long id;
		private String username;

		public JoinRespDto(User user) {
			this.id = user.getId();
			this.username = user.getUsername();
		}
	}
}
