package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.config.auth.Oauth2Properties.Provider;
import com.capturecat.core.config.auth.Oauth2Properties.Registration;
import com.capturecat.core.service.auth.IdTokenVerifierService.OidcUserPayload;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

class IdTokenVerifierServiceTest {

	private IdTokenVerifierService service;
	private Oauth2Properties props;

	// 테스트용 provider/registration 세팅
	@BeforeEach
	void setUp() {
		props = new Oauth2Properties();

		// 1. Provider 세팅
		Provider provider = new Provider();
		provider.setIssuerUri("https://test-issuer.com");
		provider.setJwkSetUri("https://test-issuer.com/keys");
		props.getProvider().put("google", provider);

		// 2. Registration 세팅
		Registration reg = new Registration();
		reg.setClientId("test-client-id");
		props.getRegistration().put("google", reg);

		// 3. 서비스 생성
		service = Mockito.spy(new IdTokenVerifierService(props));
	}

	// RSA Key 생성
	private static KeyPair generateRsaKey() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

	@DisplayName("정상 JWT 인증 테스트 (verifyJwtSignature만 stub)")
	@Test
	void verifyAndExtract_success() throws Exception {
		//given
		KeyPair kp = generateRsaKey();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.issuer("https://test-issuer.com")
			.audience("test-client-id")
			.expirationTime(new Date(System.currentTimeMillis() + 60000))
			.subject("mysub")
			.claim("email", "test@test.com")
			.claim("email_verified", true)
			.claim("name", "testNickname")
			.build();

		SignedJWT idToken = TestJwtUtil.createJwt(claims, privateKey);

		// 서명 검증만 stub (실제 공개키 HTTP, 검증 등은 생략)
		Mockito.doNothing().when(service).verifyJwtSignature(Mockito.any(), Mockito.any());

		//when
		OidcUserPayload payload = service.verifyAndExtract("google", idToken.serialize());

		//then
		assertThat(payload.provider()).isEqualTo("google");
		assertThat(payload.sub()).isEqualTo("mysub");
		assertThat(payload.email()).isEqualTo("test@test.com");
		assertThat(payload.nickname()).isEqualTo("testNickname");
		assertThat(payload.emailVerified()).isTrue();
	}

	@DisplayName("만료 토큰 예외")
	@Test
	void verifyAndExtract_expiredToken_throwsException() throws Exception {
		KeyPair kp = generateRsaKey();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.issuer("https://test-issuer.com")
			.audience("test-client-id")
			.expirationTime(new Date(System.currentTimeMillis() - 10000))
			.subject("mysub")
			.build();

		SignedJWT idToken = TestJwtUtil.createJwt(claims, privateKey);

		Mockito.doNothing().when(service).verifyJwtSignature(Mockito.any(), Mockito.any());

		assertThatThrownBy(() ->
			service.verifyAndExtract("google", idToken.serialize())
		).isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.INVALID_ID_TOKEN);
	}

	@DisplayName("iss 불일치 예외")
	@Test
	void verifyAndExtract_wrongIssuer_throwsException() throws Exception {
		KeyPair kp = generateRsaKey();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.issuer("https://WRONG-issuer.com")
			.audience("test-client-id")
			.expirationTime(new Date(System.currentTimeMillis() + 60000))
			.subject("mysub")
			.build();

		SignedJWT idToken = TestJwtUtil.createJwt(claims, privateKey);

		Mockito.doNothing().when(service).verifyJwtSignature(Mockito.any(), Mockito.any());

		assertThatThrownBy(() ->
			service.verifyAndExtract("google", idToken.serialize())
		).isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.INVALID_ID_TOKEN);
	}

	@DisplayName("audience 불일치 예외")
	@Test
	void verifyAndExtract_wrongAudience_throwsException() throws Exception {
		KeyPair kp = generateRsaKey();
		RSAPrivateKey privateKey = (RSAPrivateKey)kp.getPrivate();

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
			.issuer("https://test-issuer.com")
			.audience("WRONG-client-id")
			.expirationTime(new Date(System.currentTimeMillis() + 60000))
			.subject("mysub")
			.build();

		SignedJWT idToken = TestJwtUtil.createJwt(claims, privateKey);

		Mockito.doNothing().when(service).verifyJwtSignature(Mockito.any(), Mockito.any());

		assertThatThrownBy(() ->
			service.verifyAndExtract("google", idToken.serialize())
		).isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.INVALID_ID_TOKEN);
	}

	static class TestJwtUtil {
		static SignedJWT createJwt(JWTClaimsSet claims, RSAPrivateKey privateKey) throws Exception {
			com.nimbusds.jose.JWSSigner signer = new com.nimbusds.jose.crypto.RSASSASigner(privateKey);
			com.nimbusds.jose.JWSHeader header = new com.nimbusds.jose.JWSHeader
				.Builder(com.nimbusds.jose.JWSAlgorithm.RS256)
				.keyID("testkey")
				.build();
			SignedJWT jwt = new SignedJWT(header, claims);
			jwt.sign(signer);
			return jwt;
		}
	}
}
