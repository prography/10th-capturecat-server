package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import io.jsonwebtoken.ExpiredJwtException;

import com.capturecat.core.config.jwt.JwtUtil;
import com.capturecat.core.config.jwt.TokenType;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	String username = "testUser";
	String role = "ROLE_USER";
	long refreshTokenExpiration = 2592000000L; // 예시: 30일(ms)

	@InjectMocks
	private TokenService tokenService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	// refresh_token:{username} 포맷 사용
	private String refreshTokenKey(String username) {
		return "refresh_token:" + username;
	}

	private String blacklistKey(String token) {
		return "blacklist:" + token;
	}

	@Test
	@DisplayName("blacklistAccessToken(): accessToken을 남은 만료 시간 동안 블랙리스트로 저장한다")
	void blacklistAccessToken_SavesToRedisWithTtl() {
		// given
		String authHeader = "auth Header";
		String accessToken = "test-access-token";
		long remainMillis = 300_000L; // 5분
		given(jwtUtil.resolveToken(authHeader)).willReturn(accessToken);
		given(jwtUtil.getExpiration(accessToken)).willReturn(System.currentTimeMillis() + remainMillis);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		tokenService.blacklistAccessToken(authHeader);

		// then
		then(valueOperations).should()
			.set(eq(blacklistKey(accessToken)), eq("blacklisted"), eq(remainMillis), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	@DisplayName("isBlacklisted(): accessToken이 블랙리스트에 있으면 true, 없으면 false를 반환한다")
	void isBlacklisted_ChecksExistenceInRedis() {
		// given
		String accessToken = "test-access-token";
		given(redisTemplate.hasKey(blacklistKey(accessToken))).willReturn(true, false);

		// when & then
		assertThat(tokenService.isBlacklisted(accessToken)).isTrue();
		assertThat(tokenService.isBlacklisted(accessToken)).isFalse();
	}

	@Test
	@DisplayName("issue(): ACCESS, REFRESH 토큰을 생성하고 Redis에 저장한다")
	void issue_ShouldGenerateAndSaveTokens() {
		//given
		given(jwtUtil.generateToken(username, role, TokenType.ACCESS))
			.willReturn("accessToken");
		given(jwtUtil.generateToken(username, role, TokenType.REFRESH))
			.willReturn("refreshToken");
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// when
		Map<TokenType, String> tokenMap = tokenService.issue(username, UserRole.fromRoleString(role));

		// then
		assertThat(tokenMap)
			.containsEntry(TokenType.ACCESS, "accessToken")
			.containsEntry(TokenType.REFRESH, "refreshToken");

		then(valueOperations).should()
			.set(eq(refreshTokenKey(username)), eq("refreshToken"), eq(tokenService.refreshTokenExpiration),
				eq(TimeUnit.MILLISECONDS));
	}

	@Test
	@DisplayName("reissue(): 기존 리프레시 토큰 삭제 후 새로운 토큰을 발급한다")
	void reissue_ShouldDeleteOldAndIssueNewTokens() {
		// given
		String oldRefresh = "old-refresh";
		String newRefresh = "new-refresh";
		String newAccess = "new-access";

		String authHeader = JwtUtil.BEARER_PREFIX + oldRefresh;

		given(jwtUtil.getUsername(oldRefresh)).willReturn(username);
		given(jwtUtil.getRole(oldRefresh)).willReturn(role);
		given(jwtUtil.generateToken(username, role, TokenType.ACCESS)).willReturn(newAccess);
		given(jwtUtil.generateToken(username, role, TokenType.REFRESH)).willReturn(newRefresh);
		given(jwtUtil.isRefreshToken(oldRefresh)).willReturn(true);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(refreshTokenKey(username))).willReturn(oldRefresh);

		// when
		Map<TokenType, String> tokens = tokenService.reissue(authHeader);

		// then: 기존 refresh token 삭제
		then(redisTemplate).should().delete(refreshTokenKey(username));
		// 새 토큰 저장
		then(valueOperations).should()
			.set(eq(refreshTokenKey(username)), eq(newRefresh), eq(tokenService.refreshTokenExpiration),
				eq(TimeUnit.MILLISECONDS));
		assertThat(tokens)
			.containsEntry(TokenType.ACCESS, newAccess)
			.containsEntry(TokenType.REFRESH, newRefresh);
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
	@DisplayName("parseRefreshToken(): Redis에 토큰이 없으면 INVALID_REFRESH_TOKEN 예외 발생")
	void parseRefreshToken_NotInRedis_ThrowsInvalid() {
		String token = "some-token";
		String header = JwtUtil.BEARER_PREFIX + token;

		given(jwtUtil.isRefreshToken(token)).willReturn(true);
		given(jwtUtil.getUsername(token)).willReturn(username);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(refreshTokenKey(username))).willReturn(null);

		assertThatThrownBy(() -> tokenService.parseRefreshToken(header))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("parseRefreshToken(): 토큰 만료 시 REFRESH_TOKEN_EXPIRED 예외 발생")
	void parseRefreshToken_Expired_ThrowsExpired() {
		String token = "expired-token";
		String header = JwtUtil.BEARER_PREFIX + token;
		willThrow(new ExpiredJwtException(null, null, "expired"))
			.given(jwtUtil).isRefreshToken(token);

		assertThatThrownBy(() -> tokenService.parseRefreshToken(header))
			.isInstanceOf(CoreException.class)
			.hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_EXPIRED);
	}

	@Test
	@DisplayName("parseRefreshToken(): 유효한 헤더 및 토큰이면 토큰 값을 반환한다")
	void parseRefreshToken_Valid_ReturnsToken() {
		String token = "valid-token";
		String header = JwtUtil.BEARER_PREFIX + token;

		given(jwtUtil.isRefreshToken(token)).willReturn(true);
		given(jwtUtil.getUsername(token)).willReturn(username);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.get(refreshTokenKey(username))).willReturn(token);

		String result = tokenService.parseRefreshToken(header);

		assertThat(result).isEqualTo(token);
	}
}
