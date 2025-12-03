package com.capturecat.core.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserSettingsRepository;
import com.capturecat.core.domain.user.UserSocialAccount;
import com.capturecat.core.domain.user.UserSocialAccountRepository;
import com.capturecat.core.service.auth.SocialService.OidcUserPayload;
import com.capturecat.core.service.user.UserService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

/**
 * 서비스 클래스 이름/패키지는 실제 코드에 맞게 변경하세요.
 * 본 테스트는 다음 메서드를 대상으로 합니다.
 *  - LoginUser upsertSocialUser(OidcUserPayload payload, boolean accountLinking)
 */
@ExtendWith(MockitoExtension.class)
class SocialLoginLinkingTest {

	@Mock
	UserRepository userRepository;
	@Mock
	UserSettingsRepository userSettingsRepository;
	@Mock
	UserSocialAccountRepository userSocialAccountRepository;

	@InjectMocks
	UserService service;

	// ---- 헬퍼 ----
	private OidcUserPayload payload(String provider, String socialId) {
		OidcUserPayload p = mock(OidcUserPayload.class);
		given(p.provider()).willReturn(provider);
		given(p.socialId()).willReturn(socialId);
		return p;
	}

	private User user(long id, String username) {
		return User.builder().id(id).username(username).build();
	}

	@Nested
	@DisplayName("upsertSocialUser - 기본 동작")
	class UpsertSocialUser {

		@Test
		@DisplayName("이미 같은 provider+socialId가 링크되어 있으면 기존 유저 반환하고 저장 호출 없음")
		void returnsExistingUser_whenProviderSubAlreadyLinked() {
			// given
			OidcUserPayload kakao = payload("kakao", "k123");

			User existing = user(1L, "appleuser@example.com");
			UserSocialAccount linked = mock(UserSocialAccount.class);

			given(linked.getUser()).willReturn(existing);

			given(userSocialAccountRepository.findUserByProviderAndSocialId("kakao", "k123"))
				.willReturn(Optional.of(linked));

			// when
			LoginUser result = service.upsertSocialUser(kakao, false, null);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getUsername()).isEqualTo("appleuser@example.com");

			then(userRepository).should(never()).save(any(User.class));
			then(userSocialAccountRepository).should(never()).save(any(UserSocialAccount.class));
		}

		@Test
		@DisplayName("같은 이메일 유저 존재 + accountLinking=false → CoreException(ALREADY_REGISTERED_EMAIL)")
		void throwsWhenEmailExistsAndNotLinking() {
			// given
			OidcUserPayload kakao = payload("kakao", "k123");
			given(kakao.email()).willReturn("collision@example.com");
			given(userSocialAccountRepository.findUserByProviderAndSocialId("kakao", "k123"))
				.willReturn(Optional.empty());
			User existed = user(2L, "collision@example.com");
			given(userRepository.findByUsername("collision@example.com"))
				.willReturn(Optional.of(existed));

			// generateOrFetchUser 내부에서 provider 노출을 위해 사용
			UserSocialAccount anyExisting = mock(UserSocialAccount.class);
			given(anyExisting.getProvider()).willReturn("apple");
			given(userSocialAccountRepository.findByUser(existed)).willReturn(List.of(anyExisting));

			// when/then
			assertThatThrownBy(() -> service.upsertSocialUser(kakao, false, null))
				.isInstanceOf(CoreException.class)
				.satisfies(ex -> {
					CoreException ce = (CoreException)ex;
					assertThat(ce.getErrorType()).isEqualTo(ErrorType.ALREADY_REGISTERED_EMAIL);
				});

			then(userRepository).should(never()).save(any(User.class));
			then(userSocialAccountRepository).should(never()).save(any(UserSocialAccount.class));
		}

		@Test
		@DisplayName("같은 이메일 유저 존재 + accountLinking=true → 기존 유저에 새 소셜 계정 링크 저장")
		void linksWhenEmailExistsAndLinkingTrue() {
			// given
			OidcUserPayload kakao = payload("kakao", "k123");
			given(kakao.email()).willReturn("collision@example.com");
			given(kakao.unlinkKey()).willReturn("unlinkKakao");
			given(userSocialAccountRepository.findUserByProviderAndSocialId("kakao", "k123"))
				.willReturn(Optional.empty());

			User existed = user(3L, "collision@example.com");
			given(userRepository.findByUsername("collision@example.com"))
				.willReturn(Optional.of(existed));

			// when
			LoginUser result = service.upsertSocialUser(kakao, true, kakao.unlinkKey());

			// then
			assertThat(result).isNotNull();
			assertThat(result.getUsername()).isEqualTo("collision@example.com");

			ArgumentCaptor<UserSocialAccount> accCaptor = ArgumentCaptor.forClass(UserSocialAccount.class);
			then(userSocialAccountRepository).should().save(accCaptor.capture());

			UserSocialAccount saved = accCaptor.getValue();
			assertThat(saved.getUser()).isEqualTo(existed);
			assertThat(saved.getProvider()).isEqualTo("kakao");
			assertThat(saved.getSocialId()).isEqualTo("k123");
			assertThat(saved.getUnlinkKey()).isEqualTo("unlinkKakao");

			then(userRepository).should(never()).save(any(User.class)); // 새 유저 생성 안됨
		}

		@Test
		@DisplayName("해당 이메일 유저가 없으면 신규 유저 생성 후 소셜 계정 링크 저장")
		void createsNewUserAndLinksWhenEmailNotExists() {
			// given
			OidcUserPayload kakao = payload("kakao", "k123");
			given(kakao.email()).willReturn("newbie@example.com");
			given(kakao.unlinkKey()).willReturn("unlinkKakao");
			given(userSocialAccountRepository.findUserByProviderAndSocialId("kakao", "k123"))
				.willReturn(Optional.empty());

			given(userRepository.findByUsername("newbie@example.com"))
				.willReturn(Optional.empty());

			User persisted = user(10L, "newbie@example.com");
			// buildUser(payload)를 통해 생성된 User를 save가 반환한다고 가정
			given(userRepository.save(any(User.class))).willReturn(persisted);

			// when
			LoginUser result = service.upsertSocialUser(kakao, false, null);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getUsername()).isEqualTo("newbie@example.com");

			then(userRepository).should().save(any(User.class));

			ArgumentCaptor<UserSocialAccount> accCaptor = ArgumentCaptor.forClass(UserSocialAccount.class);
			then(userSocialAccountRepository).should().save(accCaptor.capture());

			UserSocialAccount saved = accCaptor.getValue();
			assertThat(saved.getUser()).isEqualTo(persisted);
			assertThat(saved.getProvider()).isEqualTo("kakao");
			assertThat(saved.getSocialId()).isEqualTo("k123");
			assertThat(saved.getUnlinkKey()).isEqualTo("unlinkKakao");
		}
	}
}
