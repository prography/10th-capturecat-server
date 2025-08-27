package com.capturecat.core.service.user;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserRespDto;
import com.capturecat.core.api.user.dto.UserRespDto.JoinRespDto;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.domain.user.UserSocialAccount;
import com.capturecat.core.domain.user.UserSocialAccountRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.auth.SocialService;
import com.capturecat.core.service.auth.SocialService.OidcUserPayload;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserSocialAccountRepository userSocialAccountRepository;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final BookmarkRepository bookmarkRepository;
	private final SocialService socialService;
	private final WithdrawLogService withdrawLogService;

	private final PasswordEncoder passwordEncoder;

	/**
	 * 일반 회원 가입
	 */
	@Transactional
	public JoinRespDto join(JoinReqDto joinReqDto) {
		// 기 가입 여부 검사
		if (userRepository.existsByUsername(joinReqDto.getUsername())) {
			throw new CoreException(ErrorType.JOIN_FAIL);
		}

		// 회원 가입
		User savedUser = userRepository.save(joinReqDto.toEntity(passwordEncoder));

		return new JoinRespDto(savedUser);
	}

	/**
	 * 소셜 로그인 및 신규 회원가입 처리
	 */
	@Transactional
	public LoginUser upsertSocialUser(OidcUserPayload payload, boolean accountLinking, String linkToken) {
		User userEntity =
			userSocialAccountRepository.findUserByProviderAndSocialId(payload.provider(), payload.socialId())
				.map(UserSocialAccount::getUser)
				.orElseGet(() -> {
					// 1. User 생성 or 조회 (중복이고, 연동이 아닐 경우 에러 응답)
					User user = generateOrFetchUser(payload, accountLinking);
					// 2. UserSocialAccount 생성/저장
					saveSocialAccount(payload, user, linkToken);
					return user;
				});
		return new LoginUser(userEntity);
	}

	/**
	 * 튜토리얼(시작하기) 완료 상태 저장
	 */
	@Transactional
	public void updateTutorialCompleted(LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername()) //email
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		user.tutorialComplete();
	}

	/**
	 * 회원 탈퇴
	 * 1) 모든 소셜 서비스 연결 해제 시도
	 * 2) 탈퇴 사유 저장 - 실패해도 1,2 롤백 X (별도 TX)
	 * 3) 회원 관련 데이터 삭제
	 */
	@Transactional
	public String withdraw(LoginUser loginUser, String reason) {
		User user = userRepository.findByUsername(loginUser.getUsername()) //email
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		//1. 모든 소셜 서비스 연결 해제 시도
		String resultMessage = unlinkSocials(user);

		// 2. 탈퇴 사유 저장
		withdrawLogService.save(user.getId(), reason);

		// 3. 회원 관련 데이터 삭제
		deleteUserAndRelated(user.getId());

		return resultMessage;
	}

	protected void deleteUserAndRelated(Long userId) {
		//1. 즐겨찾기 삭제
		bookmarkRepository.deleteByUserId(userId);

		// 2. 해당 User가 소유한 이미지모두 삭제
		imageTagRepository.deleteAllTagsByUserId(userId);
		imageRepository.deleteAllImagesByUserId(userId);

		// 3. User 삭제 -> social account도 삭제됨
		userRepository.deleteById(userId);
	}

	private String unlinkSocials(User user) {
		StringBuilder resultMessage = new StringBuilder();
		List<UserSocialAccount> socialAccounts = userSocialAccountRepository.findByUser(user);
		for (UserSocialAccount socialAccount : socialAccounts) {
			try {
				socialService.unlink(socialAccount.getProvider(), socialAccount.getUnlinkKey());
				// 연결 해지 성공 메시지 추가
				resultMessage.append(String.format("소셜(%s) 연결 해지 성공\n", socialAccount.getProvider()));
			} catch (Exception e) {
				// 연결 해지 실패 메시지 추가
				String msg = String.format("소셜(%s) 연결 해지 실패: unlinkKey=%s, reason=%s\n",
					socialAccount.getProvider(),
					socialAccount.getUnlinkKey(),
					e.getMessage());
				resultMessage.append(msg);
			}
		}
		return resultMessage.toString();
	}

	// 소셜 서비스 계정 정보 저장
	private void saveSocialAccount(OidcUserPayload payload, User user, String unlinkToken) {
		UserSocialAccount newAccount = UserSocialAccount.builder()
			.user(user)
			.provider(payload.provider())
			.socialId(payload.socialId())
			.unlinkKey(unlinkToken != null ? unlinkToken : payload.unlinkKey()) //최초 생성 시에만 존재
			.build();
		userSocialAccountRepository.save(newAccount);
	}

	// 이메일로 가입된 계정을 불러오거나 신규 계정 생성
	private User generateOrFetchUser(OidcUserPayload payload, boolean accountLinking) {
		User user = null;
		Optional<User> byUsername = userRepository.findByUsername(payload.email());
		// 해당 이메일로 가입된 유저가 있을 경우
		if (byUsername.isPresent()) {
			user = byUsername.get();
			// 연동이 아닐 경우 에러 응답
			if (!accountLinking) {
				String provider = userSocialAccountRepository.findByUser(user).getFirst().getProvider();
				throw new CoreException(ErrorType.ALREADY_REGISTERED_EMAIL,
					new SocialLinkingInfo(provider, payload.unlinkKey()));
			}
		} else { // 없을 경우 신규 생성
			user = userRepository.save(buildUser(payload));
		}
		return user;
	}

	/**
	 * 사용자 정보 조회
	 */
	public UserRespDto.InfoRespDto getUserInfo(String username) {
		User user = userRepository.findByUsername(username)
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		return new UserRespDto.InfoRespDto(user);
	}

	private User buildUser(OidcUserPayload payload) {
		return User.builder()
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.socialId())
			.nickname(payload.nickname())
			.email(payload.email())
			.role(UserRole.USER)
			.build();
	}

	record SocialLinkingInfo(String provider, String linkToken) {
	}
}
