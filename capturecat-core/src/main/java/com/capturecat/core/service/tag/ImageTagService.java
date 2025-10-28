package com.capturecat.core.service.tag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Service
@RequiredArgsConstructor
public class ImageTagService {

	private final ImageTagRepository imageTagRepository;
	private final UserRepository userRepository;

	@Transactional
	public void update(LoginUser loginUser, Long oldTagId, Long newTagId) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		imageTagRepository.updateImageTagsForUser(user.getId(), oldTagId, newTagId);
	}
}
