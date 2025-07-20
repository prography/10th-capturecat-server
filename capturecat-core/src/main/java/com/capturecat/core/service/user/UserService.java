package com.capturecat.core.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinRespDto;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserRole;
import com.capturecat.core.service.auth.IdTokenVerifierService;
import com.capturecat.core.service.auth.IdTokenVerifierService.OidcUserPayload;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
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
	 * TODO: PROVIDER와 SUBJECT를 기준으로 '소셜서비스'+'소셜ID' 형태로 찾는다. 각기 다른 소셜로그인으로 로그인했을 때 문제가 될 것 같다.
	 */
	public LoginUser upsertSocialUser(OidcUserPayload payload) {
		User user = userRepository.findByProviderAndSocialId(payload.provider(), payload.sub())
			.orElseGet(() -> userRepository.save(buildUser(payload)));
		return new LoginUser(user);
	}

	private User buildUser(OidcUserPayload payload) {
		return User.builder()
			.username(payload.email() != null ? payload.email() : payload.provider() + "_" + payload.sub())
			.nickname(payload.nickname())
			.email(payload.email())
			.provider(payload.provider())
			.socialId(payload.sub())
			.role(UserRole.USER)
			.build();
	}

	public void updateTutorialCompleted(LoginUser loginUser) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		user.tutorialComplete();
	}
}
