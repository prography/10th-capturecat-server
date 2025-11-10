package com.capturecat.core.service.tag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRepository;
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
	private final TagRepository tagRepository;

	@Transactional
	public void update(LoginUser loginUser, Long oldTagId, Long newTagId) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		imageTagRepository.updateImageTagsForUser(user.getId(), oldTagId, newTagId);
	}

	@Transactional
	public void delete(LoginUser loginUser, Long tagId) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(() -> new CoreException(ErrorType.TAG_NOT_FOUND));

		imageTagRepository.deleteByTagAndUser(tag, user);
	}
}
