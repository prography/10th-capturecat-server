package com.capturecat.core.service.auth;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JWSHeader; //Java용 JWT/OIDC 암호화/검증 라이브러리
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.config.auth.Oauth2Properties;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

/**
 * id_token 검증 서비스
 * OAuth2 기반의 "로그인 표준" 프로토콜 (OpenID Connect)
 */
@Service
@RequiredArgsConstructor
public class IdTokenVerifierService {
	private final Oauth2Properties oauth2Properties;

	public OidcUserPayload verifyAndExtract(String provider, String idToken) {
		// 공급자 정보 조회 (issuer, clientId, jwkSetUri 등)
		Oauth2Properties.Provider providerInfo = oauth2Properties.getProvider().get(provider);
		Oauth2Properties.Registration regInfo = oauth2Properties.getRegistration().get(provider);
		if (providerInfo == null || regInfo == null) {
			throw new CoreException(ErrorType.INVALID_ID_TOKEN);
		}

		try {
			// 1. JWT 파싱
			SignedJWT jwt = SignedJWT.parse(idToken);

			// 2. 클레임(issuer, audience, exp 등) 검증
			JWTClaimsSet claims = jwt.getJWTClaimsSet();

			if (!claims.getIssuer().equals(providerInfo.getIssuerUri())) {
				throw new RuntimeException("Invalid issuer");
			}
			if (!claims.getAudience().contains(regInfo.getClientId())) {
				throw new RuntimeException("Invalid audience");
			}
			if (claims.getExpirationTime().before(new Date())) {
				throw new RuntimeException("Token expired");
			}

			// 3. 공개키(JWK)로 서명 검증.
			JWSHeader header = jwt.getHeader(); //JWK(JSON Web Key): JWT 서명/암호화에 쓰는 "공개키"를 JSON 형식으로 표준화한 포맷
			String kid = header.getKeyID(); // 공개키 식별자(kid) 추출
			// JWKSet을 가져와서 일치하는 키 사용
			JWKSet jwkSet = JWKSet.load(URI.create(providerInfo.getJwkSetUri()).toURL());

			JWK jwk = jwkSet.getKeyByKeyId(kid);
			RSAPublicKey publicKey = ((RSAKey)jwk).toRSAPublicKey();
			JWSVerifier verifier = new RSASSAVerifier(publicKey);

			// 4. 유저 정보 반환 (provider, sub, email, email_verified)
			if (!jwt.verify(verifier)) {
				throw new RuntimeException("Invalid signature");
			}

			return new OidcUserPayload(
				provider,
				claims.getSubject(),
				claims.getStringClaim("email"),
				claims.getBooleanClaim("email_verified")
			);
		} catch (Exception e) {
			throw new CoreException(ErrorType.INVALID_ID_TOKEN);
		}
	}

	public record OidcUserPayload(
		String provider,
		String sub, // OIDC에서 각 사용자를 "서비스 내에서 고유하게 구별"하는 고유 식별자(Primary Key)
		String email,
		Boolean emailVerified
	) {
	}
}
