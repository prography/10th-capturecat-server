package com.capturecat.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.capturecat.core.domain.bookmark.Bookmark;
import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRole;

public class DummyObject {

	public static Image newMockImage(long id) {
		return Image.builder()
			.id(id)
			.fileName("test1.jpg")
			.fileUrl("testUrl1")
			.captureDate(LocalDate.now())
			.build();
	}

	public static Image newMockImage() {
		return Image.builder().fileName("test1.jpg")
			.fileUrl("testUrl1")
			.build();
	}

	public static Image newMockUserImage(long userId, long imageId) {
		return Image.builder()
			.id(imageId)
			.fileName("test1.jpg")
			.fileUrl("testUrl1")
			.user(newMockUser(userId))
			.build();
	}

	public static Image newMockUserImage(User user) {
		return Image.builder()
			.fileName("test1.jpg")
			.fileUrl("testUrl1")
			.captureDate(LocalDate.now())
			.user(user)
			.build();
	}

	public static List<Image> newMockImages(int fromId, int toId) {
		return IntStream.range(fromId, toId + 1).mapToObj(i -> {
			Image image = Image.builder().id((long)i).fileName("test" + i).fileUrl("testUrl" + i).build();
			ReflectionTestUtils.setField(image, "createdDate", LocalDateTime.now());
			ReflectionTestUtils.setField(image, "lastModifiedDate", LocalDateTime.now());
			return image;
		}).toList();
	}

	public static User newMockUser(Long id) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		return User.builder()
			.id(id)
			.username("username")
			.password(passwordEncoder.encode("password"))
			.email("username@email.com")
			.nickname("nickname")
			.build();
	}

	public static User newUser(String username) {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		String encPassword = passwordEncoder.encode("password");

		return User.builder()
			.username(username)
			.password(encPassword)
			.email(username + "@email.com")
			.nickname(username)
			.role(UserRole.USER)
			.build();
	}

	public static Bookmark newBookmark(User user, Image image) {
		return new Bookmark(user, image);
	}
}
