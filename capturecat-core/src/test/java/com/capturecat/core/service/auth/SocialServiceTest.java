package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import reactor.core.publisher.Mono;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.config.auth.Oauth2Properties.Provider;
import com.capturecat.core.config.auth.Oauth2Properties.Registration;
import com.capturecat.core.config.auth.SocialApiProperties;
import com.capturecat.core.service.auth.SocialService.OidcUserPayload;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

class SocialServiceTest {

	private SocialService service;
	private Oauth2Properties oauth2Properties;
	private SocialApiProperties socialApiProperties;
	private WebClient webClient;

	// 테스트용 provider/registration 세팅
	@BeforeEach
	void setUp() {
		oauth2Properties = new Oauth2Properties();
		// Provider 세팅
		Provider provider = new Provider();
		provider.setIssuerUri("https://test-issuer.com");
		provider.setJwkSetUri("https://test-issuer.com/keys");
		oauth2Properties.getProvider().put("google", provider);
		oauth2Properties.getProvider().put("apple", provider);

		// Registration 세팅
		Registration reg = new Registration();
		reg.setClientId("test-client-id");
		oauth2Properties.getRegistration().put("google", reg);
		oauth2Properties.getRegistration().put("apple", reg);

		// SocialApiProperties 세팅
		socialApiProperties = new SocialApiProperties();
		SocialApiProperties.Apple apple = new SocialApiProperties.Apple();
		apple.setTokenUrl("https://appleid.apple.com/auth/token");
		apple.setPrivateKeyPath("/dummy/path");
		apple.setTeamId("A1B2C3D4E5");
		apple.setKeyId("XYZ1234567");
		socialApiProperties.setApple(apple);

		// WebClient mock 준비
		webClient = Mockito.mock(WebClient.class);

		// SocialService 생성자에 WebClient 주입!
		service = Mockito.spy(new SocialService(webClient, oauth2Properties, socialApiProperties));
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
			.build();

		SignedJWT idToken = TestJwtUtil.createJwt(claims, privateKey);

		// 서명 검증만 stub (실제 공개키 HTTP, 검증 등은 생략)
		Mockito.doNothing().when(service).verifyJwtSignature(Mockito.any(), Mockito.any());

		//when
		OidcUserPayload payload = service.verifyAndExtract("google", idToken.serialize(), null, null);

		//then
		assertThat(payload.provider()).isEqualTo("google");
		assertThat(payload.socialId()).isEqualTo("mysub");
		assertThat(payload.email()).isEqualTo("test@test.com");
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
			service.verifyAndExtract("google", idToken.serialize(), null, null)
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
			service.verifyAndExtract("google", idToken.serialize(), null, null)
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
			service.verifyAndExtract("google", idToken.serialize(), null, null)
		).isInstanceOf(CoreException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.INVALID_ID_TOKEN);
	}

	//외부 API 연동(애플 등)은 실서버를 직접 호출하지 않고도 코드의 동작을 보장해야 한다.
	@DisplayName("애플 토큰 발급 fetchAppleToken 테스트")
	@Test
	void fetchAppleToken_returnsTokenMap() {
		// WebClient 체인 mocking. 각 단계별로 mock 객체를 만들고 연결.
		WebClient.RequestBodyUriSpec uriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
		WebClient.RequestBodySpec bodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
		WebClient.RequestHeadersSpec headersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
		WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

		Mockito.when(webClient.post()).thenReturn(uriSpec);
		Mockito.when(uriSpec.uri(anyString())).thenReturn(bodySpec);
		Mockito.when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
		Mockito.when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
		Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);

		Mockito.doReturn("dummyClientSecret").when(service).generateAppleClientSecret();

		// 응답값 Mocking
		Map<String, Object> fakeResponse = Map.of(
			"id_token", "dummyIdToken",
			"refresh_token", "dummyRefreshToken"
		);
		Mockito.when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(fakeResponse));

		// when
		Map result = service.fetchAppleToken("dummyAuthorizationCode");

		// then
		assertThat(result).isNotNull();
		assertThat(result.get("id_token")).isEqualTo("dummyIdToken");
		assertThat(result.get("refresh_token")).isEqualTo("dummyRefreshToken");

		// 전송 파라미터(실제 body) 검증
		ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(bodySpec).bodyValue(bodyCaptor.capture());
		String sentParams = bodyCaptor.getValue();
		assertThat(sentParams).contains("grant_type=authorization_code");
		assertThat(sentParams).contains("code=dummyAuthorizationCode");
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
