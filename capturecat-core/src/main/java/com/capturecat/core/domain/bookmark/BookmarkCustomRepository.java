package com.capturecat.core.domain.bookmark;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.user.User;

public interface BookmarkCustomRepository {

	Slice<Bookmark> searchBookmarksByUser(User user, Tag tag, Pageable pageable);
}
