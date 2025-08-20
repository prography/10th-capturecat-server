package com.capturecat.core.service.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.tag.TagFixture;
import com.capturecat.core.domain.tag.TagRepository;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.service.image.TagResponse;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private TagRepository tagRepository;

	@InjectMocks
	private SearchService searchService;

	@Test
	void 검색어_자동완성() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);
		var tags = List.of(TagFixture.createTag(1L, "자바"), TagFixture.createTag(2L, "자바스크립트"));

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(tagRepository.searchByKeyword(anyString(), anyLong(), anyInt())).willReturn(tags);

		// when
		List<TagResponse> responses = searchService.autocomplete(loginUser, "자", 10);

		// then
		assertThat(responses).hasSize(2);
	}

	@Test
	void 검색어_자동완성_시_회원이_존재하지_않으면_실패한다() {
		// given
		var user = DummyObject.newMockUser(1L);
		var loginUser = new LoginUser(user);

		given(userRepository.findByUsername(anyString())).willThrow(new CoreException(ErrorType.USER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> searchService.autocomplete(loginUser, "자", 10))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorType.USER_NOT_FOUND.getCode().getMessage());
	}
}
