package com.capturecat.core.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.api.user.dto.UserReqDto.JoinReqDto;
import com.capturecat.core.api.user.dto.UserReqDto.JoinRespDto;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.IdTokenVerifierService;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public JoinRespDto join(JoinReqDto joinReqDto) {
		// 기 가입 여부 검사
		if (userRepository.existsByUsername(joinReqDto.getUsername())) {
			throw new CoreException(ErrorType.JOIN_FAIL);
		}

		// 회원 가입
		User savedUser = userRepository.save(joinReqDto.toEntity(passwordEncoder));

		return new JoinRespDto(savedUser);
	}

	public User upsertSocialUser(IdTokenVerifierService.OidcUserPayload payload) {
		return userRepository.findByProviderAndSocialId(payload.provider(), payload.sub())
			.orElseGet(() -> {
				User user = User.builder()
					.email(payload.email())
					.provider(payload.provider())
					.socialId(payload.sub())
					.build();
				return userRepository.save(user);
			});
	}
}
