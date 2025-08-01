package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.config.auth.SocialApiProperties;
import com.capturecat.core.support.error.CoreException;

class SocialServiceGenerateAppleClientSecretTest {

	private SocialService service;
	private Oauth2Properties oauth2Properties;
	private SocialApiProperties socialApiProperties;

	@BeforeEach
	void setUp() throws Exception {
		oauth2Properties = new Oauth2Properties();
		socialApiProperties = new SocialApiProperties();

		// 1. 임시 EC 키 생성 및 .p8 파일 저장
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		keyGen.initialize(256);
		KeyPair keyPair = keyGen.generateKeyPair();
		ECPrivateKey privateKey = (ECPrivateKey)keyPair.getPrivate();

		// PKCS8 포맷 인코딩 (Base64)
		byte[] pkcs8 = privateKey.getEncoded();
		String b64 = Base64.getEncoder().encodeToString(pkcs8);
		String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
			+ b64
			+ "\n-----END PRIVATE KEY-----";
		// 임시 파일 생성
		File tmpP8 = File.createTempFile("apple-test-", ".p8");
		Files.writeString(tmpP8.toPath(), privateKeyPem);

		// 2. SocialApiProperties.Apple 세팅
		SocialApiProperties.Apple apple = new SocialApiProperties.Apple();
		apple.setPrivateKeyPath(tmpP8.getAbsolutePath());
		apple.setKeyId("TESTKEYID");
		apple.setTeamId("A1B2C3D4E5");
		apple.setTokenUrl("https://appleid.apple.com/auth/token");
		socialApiProperties.setApple(apple);

		// 3. Oauth2Properties (provider, registration) 세팅
		Oauth2Properties.Provider provider = new Oauth2Properties.Provider();
		provider.setIssuerUri("https://appleid.apple.com");
		oauth2Properties.getProvider().put("apple", provider);

		Oauth2Properties.Registration reg = new Oauth2Properties.Registration();
		reg.setClientId("test-client-id");
		oauth2Properties.getRegistration().put("apple", reg);

		// 4. 서비스 생성 (WebClient는 null, 사용하지 않음)
		service = new SocialService(null, oauth2Properties, socialApiProperties);
	}

	@DisplayName("애플 client_secret JWT 생성 정상 동작")
	@Test
	void generateAppleClientSecret_success() {
		// when
		String jwt = service.generateAppleClientSecret();

		// then
		assertThat(jwt).isNotBlank();
		// JWT 기본 구조 검증 (header.payload.signature)
		String[] parts = jwt.split("\\.");
		assertThat(parts.length).isEqualTo(3);

		// header 부분 디코딩 후 kid, alg 등 포함 여부
		String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
		assertThat(headerJson).contains("\"kid\":\"TESTKEYID\"");
		assertThat(headerJson).contains("\"alg\":\"ES256\"");
	}

	@DisplayName("잘못된 경로일 경우 CoreException 발생")
	@Test
	void generateAppleClientSecret_failOnFile() {
		// given: 잘못된 파일 경로 지정
		socialApiProperties.getApple().setPrivateKeyPath("/wrong/path/invalid.p8");
		// when, then
		assertThatThrownBy(() -> service.generateAppleClientSecret())
			.isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(com.capturecat.core.support.error.ErrorType.GENERATE_CLIENT_SECRET_FAIL);
	}
}
