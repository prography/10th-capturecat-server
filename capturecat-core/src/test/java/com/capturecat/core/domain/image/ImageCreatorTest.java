package com.capturecat.core.domain.image;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.DummyObject;
import com.capturecat.core.api.image.dto.ImageRequestDto;
import com.capturecat.core.domain.image.dto.ImageSaveRequest;
import com.capturecat.core.domain.tag.ImageTag;
import com.capturecat.core.domain.tag.ImageTagFactory;
import com.capturecat.core.domain.tag.ImageTagRepository;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.domain.user.UserRepository;
import com.capturecat.core.service.auth.LoginUser;
import com.capturecat.core.support.error.CoreException;

@ExtendWith(MockitoExtension.class) // Mockito 기능을 JUnit 5에서 사용
class ImageCreatorTest {

	@InjectMocks // 테스트 대상 클래스. @Mock으로 선언된 객체들이 자동으로 주입됩니다.
	private ImageCreator imageCreator;

	@Mock // 가짜 객체로 만들 의존성들
	private UserRepository userRepository;
	@Mock
	private ImageRepository imageRepository;
	@Mock
	private ImageTagRepository imageTagRepository;
	@Mock
	private ImageTagValidator imageTagValidator;
	@Mock
	private ImageTagFactory imageTagFactory;

	@Test
	@DisplayName("이미지와 태그 정보 저장에 성공한다")
	void createAll_success() {
		// given
		User user = DummyObject.newMockUser(1L);
		LoginUser loginUser = new LoginUser(user);

		Tag tag1 = new Tag("풍경");
		Tag tag2 = new Tag("여행");

		var saveRequest1 = ImageSaveRequest.builder().fileName("image1.jpg").build();
		var saveRequest2 = ImageSaveRequest.builder().fileName("image2.jpg").build();

		var createData1 = new ImageRequestDto.ImageCreateData(saveRequest1, of(tag1));
		var createData2 = new ImageRequestDto.ImageCreateData(saveRequest2, of(tag2));
		var requests = of(createData1, createData2);

		Image savedImage1 = DummyObject.newMockUserImage(1L, "image1.jpg", user);
		Image savedImage2 = DummyObject.newMockUserImage(2L, "image2.jpg", user);

		ImageTag imageTag1 = new ImageTag(savedImage1, tag1);
		ImageTag imageTag2 = new ImageTag(savedImage2, tag2);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
		given(imageRepository.saveAll(anyList())).willReturn(of(savedImage1, savedImage2));
		willDoNothing().given(imageTagValidator).validateTags(any(), anyList());
		given(imageTagFactory.create(savedImage1, of(tag1))).willReturn(of(imageTag1));
		given(imageTagFactory.create(savedImage2, of(tag2))).willReturn(of(imageTag2));

		// when
		List<Image> result = imageCreator.createAll(loginUser, requests);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(1L);
		assertThat(result.get(1).getId()).isEqualTo(2L);

		verify(userRepository, times(1)).findByUsername(eq(user.getUsername()));
		verify(imageRepository, times(1)).saveAll(anyList());
		verify(imageTagValidator, times(2)).validateTags(any(Image.class), anyList());
		verify(imageTagFactory, times(1)).create(savedImage1, of(tag1));
		verify(imageTagFactory, times(1)).create(savedImage2, of(tag2));
	}

	@Test
	@DisplayName("사용자를 찾지 못하면 CoreException 예외가 발생한다")
	void createAll_fail_userNotFound() {
		// given
		User user = DummyObject.newMockUser(1L);
		LoginUser loginUser = new LoginUser(user);

		given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageCreator.createAll(loginUser, Collections.emptyList()))
			.isInstanceOf(CoreException.class);

		verify(imageRepository, never()).saveAll(anyList());
		verify(imageTagRepository, never()).saveAll(anyList());
	}
}
