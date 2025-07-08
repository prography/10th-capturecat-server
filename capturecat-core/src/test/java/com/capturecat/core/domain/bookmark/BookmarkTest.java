package com.capturecat.core.domain.bookmark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;

class BookmarkTest {

	@Test
	void 북마크를_한다() {
		// given
		User user = DummyObject.newMockUser(1L);
		Image image = DummyObject.newMockImage(1L);

		// when
		Bookmark bookmark = new Bookmark(user, image);

		// then
		assertThat(bookmark).isNotNull();
	}
}
