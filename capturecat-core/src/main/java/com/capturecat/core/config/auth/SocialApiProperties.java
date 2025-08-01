package com.capturecat.core.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "social.api")
public class SocialApiProperties {
	private Apple apple;
	private Kakao kakao;

	@Getter
	@Setter
	public static class Apple {
		private String tokenUrl;
		private String revokeUrl;
		private String teamId; //Apple 개발자 계정의 팀 ID
		private String keyId;  //Apple 에서 발급한 키의 ID
		private String privateKeyPath; //Apple 에서 다운로드한 AuthKey_XXX.p8 파일 경로
	}

	@Getter
	@Setter
	public static class Kakao {
		private String userinfoUrl;
		private String unlinkUrl;
	}
}
