package com.capturecat.core.domain.tag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.capturecat.core.domain.user.User;

public interface TagCustomRepository {

	Slice<Tag> searchUserTagsByUser(User user, Pageable pageable);
}
