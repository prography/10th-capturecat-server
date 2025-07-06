package com.capturecat.core.config.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
public class Oauth2Properties {
	private final Map<String, Registration> registration = new HashMap<>();
	private final Map<String, Provider> provider = new HashMap<>();

	@Getter
	@Setter
	public static class Registration {
		private String clientId;
		private String clientSecret;
	}

	@Getter
	@Setter
	public static class Provider {
		private String issuerUri;
		private String jwkSetUri;
	}
}
