package com.capturecat.core.api.auth;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.auth.dto.SocialLoginRequest;
import com.capturecat.core.api.auth.dto.SocialLoginResponse;
import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.SocialService;
import com.capturecat.core.service.auth.SocialService.OidcUserPayload;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.service.user.UserService;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping("/v1/auth/{provider}")
@RequiredArgsConstructor
public class Oauth2AuthController {
	private final SocialService socialService;
	private final UserService userService;
	private final TokenService tokenService;

	@PostMapping("/login")
	public ResponseEntity<?> socialLogin(@PathVariable String provider, @RequestBody SocialLoginRequest requestDto) {
		// 1. provider별 id_token 검증(JWK, iss, aud 등)
		OidcUserPayload payload = socialService.verifyAndExtract(provider,
			requestDto.idToken(), requestDto.nickname(), requestDto.authToken());

		// 2. 유저 정보 추출/회원 처리
		LoginUser user = userService.upsertSocialUser(payload, requestDto.accountLinking());

		//3. JWT 발급
		Map<TokenType, String> tokenMap = tokenService.issue(user.getUsername(), user.getRole());

		//Header에 실어 응답
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + tokenMap.get(TokenType.ACCESS));
		headers.set(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + tokenMap.get(TokenType.REFRESH));

		return ResponseEntity
			.ok()
			.headers(headers)
			.body(ApiResponse.success(
				new SocialLoginResponse(user.getUsername(), user.getNickname(), user.isTutorialCompleted())));
	}
}
