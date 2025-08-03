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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
import reactor.core.publisher.Mono;

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
	private static final String KAKAO_AK_PREFIX = "KakaoAK ";

	private static final String ID_TOKEN = "id_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String BEARER_PREFIX = "Bearer ";

	public OidcUserPayload verifyAndExtract(String provider, String idToken,
		String nickname, String authToken) {
		// 공급자 정보 조회
		Provider providerInfo = oauth2Properties.getProvider().get(provider);
		Registration registrationInfo = oauth2Properties.getRegistration().get(provider);
		if (providerInfo == null || registrationInfo == null) {
			throw new CoreException(ErrorType.INVALID_ID_TOKEN);
		}

		// 1. idToken, unlinkKey 요청
		String unlinkKey = null;
		if (provider.equals(APPLE)) {
			//애플: authorization_code로 idToken과 refresh_token(최초 1회)를 얻어온다.
			if (!StringUtils.hasText(authToken)) {
				throw new CoreException(ErrorType.INVALID_AUTH_TOKEN, "apple authorization_code 누락");
			}
			Map<String, Object> response = fetchAppleToken(authToken);
			idToken = (String)response.get(ID_TOKEN);
			unlinkKey = (String)response.get(REFRESH_TOKEN);
			log.info("fetch apple refreshToken = {}", unlinkKey);
		} else if (provider.equals(KAKAO) && StringUtils.hasText(authToken)) {
			//카카오: 최초 회원가입 시 userId를 얻어온다.
			unlinkKey = fetchKakaoUserId(authToken);
			log.info("fetch kakao userId = {}", unlinkKey);
		}

		// 2. 유저 정보 추출
		try {
			// 1) idToken(JWT) 파싱
			SignedJWT jwt = SignedJWT.parse(idToken);

			// 2) 클레임(issuer, audience, exp 등) 검증
			JWTClaimsSet claims = extractClaims(jwt, providerInfo, registrationInfo);

			// 3) 공개키(JWK)로 서명 검증.
			verifyJwtSignature(jwt, providerInfo);

			// 4) 유저 정보 반환 (provider, sub, email, nickname, email_verified)
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
	String fetchKakaoUserId(String accessToken) {
		String url = socialApiProperties.getKakao().getUserinfoUrl();
		try {
			// WebClient로 API 호출
			Map response = webClient.post()
				.uri(url)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

			if (response == null || !response.containsKey("id")) {
				throw new CoreException(ErrorType.FETCH_SOCIAL_TOKEN_FAIL,
					response != null ? "[kakaoUserId] error response: " + response : "No response");
			}

			// Kakao의 userId는 Long(숫자)이므로 String 변환
			Object id = response.get("id");
			return String.valueOf(id);
		} catch (WebClientResponseException e) {
			// 실제 에러 body 추출해서 그대로 전달
			throw new CoreException(ErrorType.SOCIAL_API_ERROR, e.getResponseBodyAsString());
		}
	}

	/**
	 * 애플: authToken(authorization_code) → idToken, refresh_token(최초 1회) 획득
	 */
	Map fetchAppleToken(String authorizationCode) {
		String url = socialApiProperties.getApple().getTokenUrl();

		// 파라미터 Map 생성
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("code", authorizationCode);
		params.add("client_id", oauth2Properties.getRegistration().get(APPLE).getClientId());
		params.add("client_secret", generateAppleClientSecret());
		log.info("Apple /token params: {}", params.get("client_secret"));

		// 토큰 요청
		return webClient.post()
			.uri(url)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.body(BodyInserters.fromFormData(params))
			.retrieve()
			.onStatus(
				status -> status.is4xxClientError() || status.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.flatMap(errorBody -> Mono.error(new CoreException(ErrorType.FETCH_SOCIAL_TOKEN_FAIL,
						"[AppleToken] error response: " + errorBody))))
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

	/**
	 * 소셜 로그인 연결 해제
	 * 베스트 에포트 패턴: 할 수 있는 만큼(최대한) 시도하고, 실패해도 전체 트랜잭션을 되돌리지 않고 오류만 로그로 남긴다.
	 */
	public void unlink(String provider, String unlinkKey) {
		switch (provider) {
			case KAKAO -> unlinkKaKaoUser(unlinkKey);
			case APPLE -> revokeAppleUser(unlinkKey);
		}
	}

	private void revokeAppleUser(String refreshToken) {
		String url = socialApiProperties.getApple().getRevokeUrl();

		// Apple client_id, client_secret(=JWT)는 반드시 서버에서 동적으로 생성
		String clientId = oauth2Properties.getRegistration().get(APPLE).getClientId();
		String clientSecret = generateAppleClientSecret();

		// 파라미터 구성 (x-www-form-urlencoded)
		String params = String.format(
			"client_id=%s&client_secret=%s&token=%s&token_type_hint=refresh_token",
			clientId, clientSecret, refreshToken
		);

		// API 호출
		Map response = webClient.post()
			.uri(url)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.bodyValue(params)
			.retrieve()
			.bodyToMono(Map.class)
			.block();

		if (response == null) {
			throw new CoreException(ErrorType.UNLINK_SOCIAL_FAIL, "Apple revoke 응답이 없습니다.");
		}
	}

	private void unlinkKaKaoUser(String kakaoUserId) {
		String url = socialApiProperties.getKakao().getUnlinkUrl();
		String adminKey = socialApiProperties.getKakao().getServiceAppAdminKey();

		// 파라미터 구성
		String params = String.format("target_id_type=user_id&target_id=%s", kakaoUserId);

		// API 호출
		Map response = webClient.post()
			.uri(url)
			.header(HttpHeaders.AUTHORIZATION, KAKAO_AK_PREFIX + adminKey)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.bodyValue(params)
			.retrieve()
			.bodyToMono(Map.class)
			.block();

		// 정상적으로 "id" 반환 시 성공, 아니면 예외 처리
		if (response == null || !response.containsKey("id")) {
			throw new CoreException(ErrorType.UNLINK_SOCIAL_FAIL, "카카오 연동 해제 실패: " + response);
		}
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
