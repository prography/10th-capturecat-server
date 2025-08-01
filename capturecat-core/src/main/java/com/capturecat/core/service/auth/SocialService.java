package com.capturecat.core.service.auth;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.config.auth.Oauth2Properties.Provider;
import com.capturecat.core.config.auth.Oauth2Properties.Registration;
import com.capturecat.core.config.auth.SocialApiProperties;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

/**
 * id_token 검증 서비스
 * OAuth2 기반의 "로그인 표준" 프로토콜 (OpenID Connect)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialService {
	private final WebClient webClient;
	private final Oauth2Properties oauth2Properties;
	private final SocialApiProperties socialApiProperties;
	private static final String KAKAO = "kakao";
	private static final String APPLE = "apple";
	private static final String GOOGLE = "google";
	private static final String EC_KEY_ALGORITHM = "EC";
	private static final String ID_TOKEN = "id_token";
	private static final String REFRESH_TOKEN = "refresh_token";

	public OidcUserPayload verifyAndExtract(String provider, String idToken,
		String nickname, String authToken) {
		// 공급자 정보 조회
		Provider providerInfo = oauth2Properties.getProvider().get(provider);
		Registration registrationInfo = oauth2Properties.getRegistration().get(provider);
		if (providerInfo == null || registrationInfo == null) {
			throw new CoreException(ErrorType.INVALID_ID_TOKEN);
		}

		// 최초 회원가입 시 소셜 연결 해제를 위한 정보를 얻어온다.
		String unlinkKey = null;
		if (provider.equals(APPLE)) {
			//애플: authorization_code로 idToken과 refresh_token(최초 1회)를 얻어온다.
			if (!StringUtils.hasText(authToken)) {
				throw new CoreException(ErrorType.INVALID_AUTH_TOKEN);
			}
			Map<String, Object> response = fetchAppleToken(authToken);
			idToken = (String)response.get(ID_TOKEN);
			unlinkKey = (String)response.get(REFRESH_TOKEN);
		} else if (provider.equals(KAKAO) && StringUtils.hasText(authToken)) {
			//카카오: 최초 회원가입 시 userId를 얻어온다.
			unlinkKey = fetchKakaoUserId(provider, authToken);
		}

		try {
			// 1. idToken(JWT) 파싱
			SignedJWT jwt = SignedJWT.parse(idToken);
			// 2. 클레임(issuer, audience, exp 등) 검증
			JWTClaimsSet claims = extractClaims(jwt, providerInfo, registrationInfo);

			// 3. 공개키(JWK)로 서명 검증.
			verifyJwtSignature(jwt, providerInfo);

			// 5. 유저 정보 반환 (provider, sub, email, nickname, email_verified)
			return new OidcUserPayload(
				provider,
				claims.getSubject(),
				claims.getStringClaim("email"),
				extractNickname(claims, provider, nickname),
				unlinkKey, //null일 경우 update X
				claims.getBooleanClaim("email_verified")
			);
		} catch (Exception e) {
			throw new CoreException(ErrorType.INVALID_ID_TOKEN);
		}
	}

	/**
	 * 카카오: authToken(access_token) → userId 획득
	 */
	private String fetchKakaoUserId(String provider, String accessToken) {
		return null;
	}

	/**
	 * 애플: authToken(authorization_code) → idToken, refresh_token(최초 1회) 획득
	 */
	Map fetchAppleToken(String authorizationCode) {
		String url = socialApiProperties.getApple().getTokenUrl();

		// application/x-www-form-urlencoded 파라미터 구성
		String params = String.format(
			"grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s",
			authorizationCode,
			oauth2Properties.getRegistration().get(APPLE).getClientId(),
			generateAppleClientSecret()
		);

		// 토큰 요청
		return webClient.post()
			.uri(url)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.bodyValue(params)
			.retrieve()
			.bodyToMono(Map.class)
			.block();
	}

	//

	/**
	 * client_secret(JWT) 생성
	 * client_secret: 우리가 "애플 개발자 포털에 등록된 진짜 서비스 서버"임을 증명하는 역할
	 */
	String generateAppleClientSecret() {
		SocialApiProperties.Apple apple = socialApiProperties.getApple();
		String issuerUri = oauth2Properties.getProvider().get(APPLE).getIssuerUri();
		String clientId = oauth2Properties.getRegistration().get(APPLE).getClientId();

		try {
			// .p8 키 파일 Base64 디코딩
			String privateKeyContent = Files.readString(Paths.get(apple.getPrivateKeyPath()))
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s+", "");
			byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyContent);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
			KeyFactory kf = KeyFactory.getInstance(EC_KEY_ALGORITHM);
			PrivateKey privateKey = kf.generatePrivate(keySpec);

			Instant now = Instant.now();
			Instant exp = now.plusSeconds(60 * 60 * 6);

			return Jwts.builder()
				.header().add("kid", apple.getKeyId()).and()
				.issuer(apple.getTeamId())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claim("aud", issuerUri)
				.subject(clientId)
				.signWith(privateKey)
				.compact();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new CoreException(ErrorType.GENERATE_CLIENT_SECRET_FAIL);
		}
	}

	void verifyJwtSignature(SignedJWT jwt, Provider providerInfo)
		throws IOException, ParseException, JOSEException {
		//JWK(JSON Web Key): JWT 서명/암호화에 쓰는 "공개키"를 JSON 형식으로 표준화한 포맷
		JWSHeader header = jwt.getHeader();
		String kid = header.getKeyID(); // 공개키 식별자(kid) 추출
		// JWKSet을 가져와서 일치하는 키 사용
		JWKSet jwkSet = JWKSet.load(URI.create(providerInfo.getJwkSetUri()).toURL());

		JWK jwk = jwkSet.getKeyByKeyId(kid);
		RSAPublicKey publicKey = ((RSAKey)jwk).toRSAPublicKey();
		JWSVerifier verifier = new RSASSAVerifier(publicKey);

		if (!jwt.verify(verifier)) {
			throw new RuntimeException("Invalid signature");
		}
	}

	JWTClaimsSet extractClaims(SignedJWT jwt, Provider providerInfo, Registration regInfo)
		throws ParseException {
		JWTClaimsSet claims = jwt.getJWTClaimsSet();

		log.info("claims.getIssuer(): {}", claims.getIssuer());
		log.info("claims.getAudience(): {}", claims.getAudience());
		log.info("claims.getExpirationTime(): {}", claims.getExpirationTime());
		if (!claims.getIssuer().equals(providerInfo.getIssuerUri())) {
			throw new RuntimeException("Invalid issuer");
		}
		if (!claims.getAudience().contains(regInfo.getClientId())) {
			throw new RuntimeException("Invalid audience");
		}
		if (claims.getExpirationTime().before(new Date())) {
			throw new RuntimeException("Token expired");
		}
		return claims;
	}

	private String extractNickname(JWTClaimsSet claims, String provider, String nickname) throws ParseException {
		return switch (provider) {
			case KAKAO -> claims.getStringClaim("nickname");
			case GOOGLE -> claims.getStringClaim("name");
			case APPLE -> nickname;
			default -> claims.getStringClaim("email"); //apple의 경우 requestDto를 통해 받는다.
		};
	}

	public record OidcUserPayload(
		String provider,
		String socialId, // OIDC에서 각 사용자를 "서비스 내에서 고유하게 구별"하는 고유 식별자(Primary Key)
		String email,
		String nickname,
		String unlinkKey,
		Boolean emailVerified
	) {
	}
}
