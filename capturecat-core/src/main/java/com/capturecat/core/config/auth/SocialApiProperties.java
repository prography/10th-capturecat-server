package com.capturecat.core.config.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Valid
@Component
@ConfigurationProperties(prefix = "social.api")
public class SocialApiProperties {
	@Valid
	private Apple apple;
	@Valid
	private Kakao kakao;

	@Getter
	@Setter
	public static class Apple {
		@NotBlank
		private String tokenUrl;
		@NotBlank
		private String revokeUrl;
		@NotBlank
		private String teamId; //Apple 개발자 계정의 팀 ID
		@NotBlank
		private String keyId;  //Apple 에서 발급한 키의 ID
		@NotBlank
		private String privateKeyPath; //Apple 에서 다운로드한 AuthKey_XXX.p8 파일 경로
	}

	@Getter
	@Setter
	public static class Kakao {
		@NotBlank
		private String userinfoUrl;
		@NotBlank
		private String unlinkUrl;
		@NotBlank
		private String serviceAppAdminKey;
	}
}
