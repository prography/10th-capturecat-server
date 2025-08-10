package com.capturecat.core.api.user.dto;

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
}
