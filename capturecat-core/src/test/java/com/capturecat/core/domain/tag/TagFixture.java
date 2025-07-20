package com.capturecat.core.domain.tag;

import org.springframework.test.util.ReflectionTestUtils;

public class TagFixture {

	public static Tag createTag(Long id, String name) {
		Tag tag = new Tag(name);
		ReflectionTestUtils.setField(tag, "id", id);
		return tag;
	}
}
