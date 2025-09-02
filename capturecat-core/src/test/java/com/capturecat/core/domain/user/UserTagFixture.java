package com.capturecat.core.domain.user;

import org.springframework.test.util.ReflectionTestUtils;

import com.capturecat.core.domain.tag.Tag;

public class UserTagFixture {

	public static UserTag createUserTag(Long id, User user, Tag tag) {
		UserTag userTag = UserTag.create(user, tag);
		ReflectionTestUtils.setField(userTag, "id", id);
		return userTag;
	}
}
