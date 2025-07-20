package com.capturecat.core.domain.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.capturecat.core.domain.user.User;

public interface TagCustomRepository {

	Slice<Tag> searchUserTagsByUser(User user, Pageable pageable);

	Slice<Tag> searchByRelatedTags(User user, List<String> tagNames, Pageable pageable);

	Slice<Tag> searchMostUsedTagsByUser(User user, Pageable pageable);
}
