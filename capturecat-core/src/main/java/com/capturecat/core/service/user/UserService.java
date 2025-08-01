package com.capturecat.core.service.user;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinRespDto;
import com.capturecat.core.domain.bookmark.BookmarkRepository;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.domain.user.UserSocialAccount;
import com.capturecat.core.domain.user.UserSocialAccountRepository;
import com.capturecat.core.service.auth.IdTokenVerifierService.OidcUserPayload;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserSocialAccountRepository userSocialAccountRepository;
	private final ImageRepository imageRepository;
	private final ImageTagRepository imageTagRepository;
	private final BookmarkRepository bookmarkRepository;

	private final PasswordEncoder passwordEncoder;

	/**
	 * 일반 회원 가입
	 */
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
	 * 소셜 로그인 시 회원가입 처리
	 */
	public LoginUser upsertSocialUser(OidcUserPayload payload) {
		User user = userSocialAccountRepository.findUserByProviderAndSocialId(payload.provider(), payload.socialId())
			.map(UserSocialAccount::getUser)
			.orElseGet(() -> {
				// 1. User 생성/저장
				User newUser = userRepository.save(buildUser(payload));
				// 2. UserSocialAccount 생성/저장
				UserSocialAccount newAccount = UserSocialAccount.builder()
					.user(newUser)
					.provider(payload.provider())
					.socialId(payload.socialId())
					.build();
				userSocialAccountRepository.save(newAccount);
				return newUser;
			});
		return new LoginUser(user);
	}

	/**
	 * 튜토리얼(시작하기) 완료 상태 저장
	 */
	public void updateTutorialCompleted(LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername()) //email
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		user.tutorialComplete();
	}

	/**
	 * 회원 탈퇴
	 */
	public void withdraw(LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername()) //email
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		//1. 즐겨찾기 삭제
		bookmarkRepository.deleteByUser(user);

		// 2. 해당 User가 소유한 이미지 모두 삭제
		List<Image> byUser = imageRepository.findByUser(user);
		byUser.forEach(imageTagRepository::deleteAllByImage);
		imageRepository.deleteAll(byUser);

		// 2. User 삭제
		userRepository.delete(user);
	}

	private User buildUser(OidcUserPayload payload) {
		return User.builder()
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.socialId())
			.nickname(payload.nickname())
			.email(payload.email())
			.role(UserRole.USER)
			.build();
	}
}
