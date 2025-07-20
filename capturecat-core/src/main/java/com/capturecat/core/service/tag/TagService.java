package com.capturecat.core.service.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getTags(LoginUser loginUser, Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<Tag> tags = tagRepository.searchUserTagsByUser(user, pageable);
		Slice<TagResponse> responses = tags.map(TagResponse::from);

		return CursorUtil.toCursorResponse(responses, TagResponse::id);
	}

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getRelatedTags(LoginUser loginUser, List<String> tagNames, Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<Tag> tags = tagRepository.searchByRelatedTags(user, tagNames, pageable);
		Slice<TagResponse> responses = tags.map(TagResponse::from);

		return CursorUtil.toCursorResponse(responses, TagResponse::id);
	}

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getMostUsedTags(LoginUser loginUser, Pageable pageable) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<Tag> tags = tagRepository.searchMostUsedTagsByUser(user, pageable);
		Slice<TagResponse> responses = tags.map(TagResponse::from);

		return CursorUtil.toCursorResponse(responses, TagResponse::id);
	}
}
