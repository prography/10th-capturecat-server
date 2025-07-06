package com.capturecat.core.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.capturecat.core.DummyObject;

class UserTest {

	@Test
	void 회원_동등성_테스트() {
		// given
		User user1 = DummyObject.newMockUser(1L);
		User user2 = DummyObject.newMockUser(1L);
		User user3 = DummyObject.newMockUser(2L);

		// when
		boolean equals = user1.equals(user2);
		boolean notEquals = user1.equals(user3);

		// then
		assertThat(equals).isTrue();
		assertThat(notEquals).isFalse();
	}
}
