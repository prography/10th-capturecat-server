package com.capturecat.core.service.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.config.auth.LoginUser;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;
import com.capturecat.core.support.response.CursorResponse;

@Service
@RequiredArgsConstructor
public class TagService {

	private final TagRepository tagRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public CursorResponse<TagResponse> getTags(Pageable pageable) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		LoginUser loginUser = (LoginUser)authentication.getPrincipal();
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

		Slice<Tag> tags = tagRepository.searchUserTagsByUser(user, pageable);
		Slice<TagResponse> responses = tags.map(TagResponse::from);

		if (responses.isEmpty()) {
			return CursorResponse.empty();
		} else {
			Long lastCursor = responses.getContent().getLast().id();
			return CursorResponse.of(responses, lastCursor);
		}
	}
}
