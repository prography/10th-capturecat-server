package com.capturecat.core.api.auth;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.service.auth.TokenService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.response.ApiResponse;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

	private final TokenService tokenIssueService;

	@PostMapping("/reissue")
	public ResponseEntity<?> reissue(
		@RequestHeader(value = JwtUtil.REFRESH_TOKEN_HEADER, required = false) String authHeader) {

		Map<TokenType, String> newTokenMap;
		try {
			//Access, Refresh token 재발행
			newTokenMap = tokenIssueService.reissue(authHeader);
		} catch (CoreException e) {
			return ResponseEntity
				.status(e.getErrorType().getStatus())
				.body(ApiResponse.error(e.getErrorType()));
		}

		//헤더에 토큰을 담아 응답
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, JwtUtil.BEARER_PREFIX + newTokenMap.get(TokenType.ACCESS));
		headers.set(JwtUtil.REFRESH_TOKEN_HEADER, JwtUtil.BEARER_PREFIX + newTokenMap.get(TokenType.REFRESH));

		return ResponseEntity
			.ok()
			.headers(headers)
			.body(ApiResponse.success());
	}
}
