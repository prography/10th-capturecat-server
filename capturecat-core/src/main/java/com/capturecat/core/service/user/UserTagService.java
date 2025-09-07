package com.capturecat.core.service.user;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRegister;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.domain.user.UserTag;
import com.capturecat.core.domain.user.UserTagRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.CursorResponse;
import com.capturecat.core.support.util.CursorUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTagService {

	private static final int MAX_USER_TAG_COUNT = 30;

	private final UserRepository userRepository;
	private final UserTagRepository userTagRepository;
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

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getAll(LoginUser loginUser, Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Slice<UserTag> userTags = userTagRepository.findAllByUser(user, pageable);

		List<TagResponse> tags = userTags.stream()
			.map(ut -> TagResponse.from(ut.getTag()))
			.toList();

		return CursorUtil.toCursorResponse(tags, userTags.hasNext(), TagResponse::id);
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
