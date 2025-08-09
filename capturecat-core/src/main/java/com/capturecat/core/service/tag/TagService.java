package com.capturecat.core.service.tag;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.CursorResponse;
import com.capturecat.core.support.util.CursorUtil;

@Service
@RequiredArgsConstructor
public class TagService {

	private final TagRepository tagRepository;
	private final UserRepository userRepository;
	private final ImageTagRepository imageTagRepository;

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getTags(LoginUser loginUser, Pageable pageable) {
		return searchTagsWithCursor(loginUser, user -> tagRepository.searchUserTagsByUser(user, pageable));
	}

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getRelatedTags(LoginUser loginUser, List<String> tagNames, Pageable pageable) {
		return searchTagsWithCursor(loginUser, user -> tagRepository.searchByRelatedTags(user, tagNames, pageable));
	}

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getMostUsedTags(LoginUser loginUser, Pageable pageable) {
		return searchTagsWithCursor(loginUser, user -> tagRepository.searchMostUsedTagsByUser(user, pageable));
	}

	@Transactional
	public TagResponse deleteTag(LoginUser loginUser, Long tagId) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		Tag tag = tagRepository.findById(tagId)
			.orElseThrow(() -> new CoreException(ErrorType.TAG_NOT_FOUND));

		imageTagRepository.deleteTagAndUser(tag, user);

		return TagResponse.from(tag);
	}

	private CursorResponse<TagResponse> searchTagsWithCursor(LoginUser loginUser,
		Function<User, Slice<Tag>> tagSearchFunction) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<Tag> tags = tagSearchFunction.apply(user);
		Slice<TagResponse> responses = tags.map(TagResponse::from);

		return CursorUtil.toCursorResponse(responses, TagResponse::id);
	}
}
