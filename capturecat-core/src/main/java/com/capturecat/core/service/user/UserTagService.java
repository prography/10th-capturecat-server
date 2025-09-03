package com.capturecat.core.service.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserTag;
import com.capturecat.core.domain.user.UserTagRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTagService {

	private static final int MAX_USER_TAG_COUNT = 30;

	private final UserRepository userRepository;
	private final UserTagRepository userTagRepository;
	private final TagRepository tagRepository;
	private final TagRegister tagRegister;

	@Transactional
	public TagResponse create(LoginUser loginUser, String tagName) {
		try {
			User user = userRepository.findByUsername(loginUser.getUsername())
				.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
			Tag tag = tagRegister.registerTagsFor(tagName);

			validate(user, tag);

			userTagRepository.save(UserTag.create(user, tag));

			return TagResponse.from(tag);
		} catch (DataIntegrityViolationException ex) {
			throw new CoreException(ErrorType.USER_TAG_ALREADY_EXISTS);
		}
	}

	@Transactional
	public TagResponse update(LoginUser loginUser, Long currentTagId, String newTagName) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Tag currentTag = tagRepository.findById(currentTagId)
			.orElseThrow(() -> new CoreException(ErrorType.TAG_NOT_FOUND));
		UserTag userTag = userTagRepository.findByUserAndTag(user, currentTag)
			.orElseThrow(() -> new CoreException(ErrorType.USER_TAG_NOT_FOUND));
		Tag newTag = tagRegister.registerTagsFor(newTagName);

		validateDuplicateUserTag(user, newTag);

		userTagRepository.delete(userTag);
		userTagRepository.save(UserTag.create(user, newTag));

		return TagResponse.from(newTag);
	}

	private void validate(User user, Tag tag) {
		validateDuplicateUserTag(user, tag);
		validateUserTagCountLimit(user);
	}

	private void validateDuplicateUserTag(User user, Tag tag) {
		if (userTagRepository.existsByUserAndTag(user, tag)) {
			throw new CoreException(ErrorType.USER_TAG_ALREADY_EXISTS);
		}
	}

	private void validateUserTagCountLimit(User user) {
		long userTagCount = userTagRepository.countByUser(user);

		if (userTagCount >= MAX_USER_TAG_COUNT) {
			throw new CoreException(ErrorType.TOO_MANY_USER_TAGS);
		}
	}
}
