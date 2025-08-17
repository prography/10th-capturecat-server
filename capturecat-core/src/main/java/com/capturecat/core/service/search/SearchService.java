package com.capturecat.core.service.search;

import java.util.List;

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

@Service
@RequiredArgsConstructor
public class SearchService {

	private final UserRepository userRepository;
	private final TagRepository tagRepository;

	@Transactional(readOnly = true)
	public List<TagResponse> autocomplete(LoginUser loginUser, String keyword, int size) {
		User user = userRepository.findByUsername(loginUser.getUsername())
			.orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));
		List<Tag> tags = tagRepository.searchByKeyword(keyword, user.getId(), size);

		return tags.stream()
			.map(TagResponse::from)
			.toList();
	}
}
