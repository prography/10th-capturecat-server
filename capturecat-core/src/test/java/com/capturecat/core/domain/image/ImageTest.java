package com.capturecat.core.domain.image;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.capturecat.core.DummyObject;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorCode;

class ImageTest {

	@Test
	void 이미지의_소유자이면_예외가_발생하지_않는다() {
		// given
		Image image = DummyObject.newMockUserImage(1L, 1L);
		User user = DummyObject.newMockUser(1L);

		// when & then
		assertThatNoException().isThrownBy(() -> image.validateOwnership(user));
	}

	@Test
	void 이미지의_소유자가_아니면_예외가_발생한다() {
		// given
		Image image = DummyObject.newMockUserImage(1L, 1L);
		User user = DummyObject.newMockUser(2L);

		// when & then
		assertThatThrownBy(() -> image.validateOwnership(user))
			.isInstanceOf(CoreException.class)
			.hasMessage(ErrorCode.IMAGE_ACCESS_DENIED.getMessage());
	}
}
