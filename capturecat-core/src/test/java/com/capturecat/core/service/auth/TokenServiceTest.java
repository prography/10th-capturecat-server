package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.ExpiredJwtException;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.auth.RefreshToken;
import com.capturecat.core.domain.auth.RefreshTokenRepository;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	String username = "testUser";
	String role = "ROLE_USER";

	@InjectMocks
	private TokenService tokenService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	@DisplayName("issue(): ACCESS, REFRESH 토큰을 생성하고 DB에 저장한다")
	void issue_ShouldGenerateAndSaveTokens() {
		//given
		given(jwtUtil.generateToken(username, role, TokenType.ACCESS))
			.willReturn("accessToken");
		given(jwtUtil.generateToken(username, role, TokenType.REFRESH))
			.willReturn("refreshToken");

		// when
		Map<TokenType, String> tokenMap = tokenService.issue(username, role);

		// then
		assertThat(tokenMap)
			.containsEntry(TokenType.ACCESS, "accessToken")
			.containsEntry(TokenType.REFRESH, "refreshToken");

		then(refreshTokenRepository).should().save(argThat((RefreshToken t) ->
			username.equals(t.getUsername()) && "refreshToken".equals(t.getRefreshToken())));
	}

	@Test
	@DisplayName("reissue(): 기존 리프레시 토큰 삭제 후 새로운 토큰을 발급한다")
	void reissue_ShouldDeleteOldAndIssueNewTokens() {
		// given
		String oldRefresh = "old-refresh";
		given(jwtUtil.getUsername(oldRefresh)).willReturn(username);
		given(jwtUtil.getRole(oldRefresh)).willReturn(role);
		given(jwtUtil.generateToken(username, role, TokenType.ACCESS)).willReturn("new-access");
		given(jwtUtil.generateToken(username, role, TokenType.REFRESH)).willReturn("new-refresh");
		given(jwtUtil.isRefreshToken(oldRefresh)).willReturn(true);
		given(refreshTokenRepository.existsByRefreshToken(oldRefresh)).willReturn(true);

		// when
		Map<TokenType, String> tokens = tokenService.reissue(JwtUtil.BEARER_PREFIX + oldRefresh);

		// then
		then(refreshTokenRepository).should().deleteByRefreshToken(oldRefresh);
		assertThat(tokens)
			.containsEntry(TokenType.ACCESS, "new-access")
			.containsEntry(TokenType.REFRESH, "new-refresh");
		then(refreshTokenRepository).should().save(argThat((RefreshToken t) ->
			username.equals(t.getUsername()) && "new-refresh".equals(t.getRefreshToken())));
	}

	@Test
	@DisplayName("parseRefreshToken(): 헤더가 null이면 INVALID_REFRESH_TOKEN 예외 발생")
	void parseRefreshToken_NullHeader_ThrowsInvalid() {
		assertThatThrownBy(() -> tokenService.parseRefreshToken(null))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("parseRefreshToken(): 잘못된 프리픽스이면 INVALID_REFRESH_TOKEN 예외 발생")
	void parseRefreshToken_InvalidPrefix_ThrowsInvalid() {
		String badHeader = "Token abc";
		assertThatThrownBy(() -> tokenService.parseRefreshToken(badHeader))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("parseRefreshToken(): DB에 토큰이 없으면 INVALID_REFRESH_TOKEN 예외 발생")
	void parseRefreshToken_NotInRepository_ThrowsInvalid() {
		String header = JwtUtil.BEARER_PREFIX + "some-token";
		given(jwtUtil.isRefreshToken("some-token")).willReturn(true);
		given(refreshTokenRepository.existsByRefreshToken("some-token")).willReturn(false);

		assertThatThrownBy(() -> tokenService.parseRefreshToken(header))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("parseRefreshToken(): 토큰 만료 시 REFRESH_TOKEN_EXPIRED 예외 발생")
	void parseRefreshToken_Expired_ThrowsExpired() {
		String header = JwtUtil.BEARER_PREFIX + "expired-token";
		willThrow(new ExpiredJwtException(null, null, "expired"))
			.given(jwtUtil).isRefreshToken("expired-token");

		assertThatThrownBy(() -> tokenService.parseRefreshToken(header))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_EXPIRED);
	}

	@Test
	@DisplayName("parseRefreshToken(): 유효한 헤더 및 토큰이면 토큰 값을 반환한다")
	void parseRefreshToken_Valid_ReturnsToken() {
		String header = JwtUtil.BEARER_PREFIX + "valid-token";
		given(jwtUtil.isRefreshToken("valid-token")).willReturn(true);
		given(refreshTokenRepository.existsByRefreshToken("valid-token")).willReturn(true);
		// isExpired 호출 시 예외 없이 통과
		// when
		String result = tokenService.parseRefreshToken(header);

		// then
		assertThat(result).isEqualTo("valid-token");
	}
}
