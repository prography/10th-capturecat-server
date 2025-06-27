package com.capturecat.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;

public class DummyObject {

	public static Image newMockImage(int id) {
		return Image.builder().id((long)id).fileName("test1.jpg").fileUrl("testUrl1").build();
	}

	public static List<Image> newMockImages(int fromId, int toId) {
		return IntStream.range(fromId, toId + 1).mapToObj(i -> {
			Image image = Image.builder().id((long)i).fileName("test" + i).fileUrl("testUrl" + i).build();
			ReflectionTestUtils.setField(image, "createdDate", LocalDateTime.now());
			ReflectionTestUtils.setField(image, "lastModifiedDate", LocalDateTime.now());
			return image;
		}).toList();
	}

	protected User newMockUser(Long id) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		return User.builder()
			.id(id)
			.username("username")
			.password(passwordEncoder.encode("password"))
			.email("username@email.com")
			.build();
	}

}
