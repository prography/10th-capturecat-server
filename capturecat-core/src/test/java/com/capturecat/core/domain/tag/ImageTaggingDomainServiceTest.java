package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageTaggingDomainServiceTest {

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private ImageTagRepository imageTagRepository;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private ImageTagFactory imageTagFactory;

	@InjectMocks
	private ImageTaggingDomainService imageTaggingDomainService;

	private Image image1;
	private Image image2;
	private List<Image> imagesToSave;

	@BeforeEach
	void setUp() {
		image1 = Image.builder().id(1L).fileName("img1.jpg").fileUrl("url1").build();
		image2 = Image.builder().id(2L).fileName("img2.png").fileUrl("url2").build();
		imagesToSave = List.of(image1, image2);
	}

	@Test
	void 모든_태그가_신규일_때_이미지와_태그를_성공적으로_등록해야_한다() {
		// given
		List<String> requestTagNames = List.of("newTag1", "newTag2");
		List<Tag> expectedNewTags = List.of(
			new Tag("newTag1"), new Tag("newTag2")
		);

		List<ImageTag> expectedImageTags = List.of(
			new ImageTag(image1, expectedNewTags.get(0)),
			new ImageTag(image1, expectedNewTags.get(1)),
			new ImageTag(image2, expectedNewTags.get(0)),
			new ImageTag(image2, expectedNewTags.get(1))
		);

		given(imageRepository.saveAll(anyList())).willReturn(imagesToSave);
		given(tagRepository.findByNameIn(anyList())).willReturn(Collections.emptyList());
		given(tagRepository.saveAll(anyList())).willReturn(expectedNewTags);
		given(imageTagFactory.create(any(), anyList())).willReturn(expectedImageTags);
		given(imageTagRepository.saveAll(anyList())).willReturn(expectedImageTags);

		// when
		imageTaggingDomainService.registerNewImagesWithTags(imagesToSave, requestTagNames);

		// then
		then(tagRepository).should(times(1)).saveAll(anyList());
	}

	@Test
	void 모든_태그가_기존일_때_태그를_생성하지_않고_이미지와_태그를_성공적으로_등록해야_한다() {
		// given
		List<String> requestTagNames = List.of("existingTag1", "existingTag2");
		List<Tag> existingTags = List.of(
			new Tag(1L, "existingTag1"),
			new Tag(2L, "existingTag2")
		);

		List<ImageTag> expectedImageTags = List.of(
			new ImageTag(image1, existingTags.get(0)),
			new ImageTag(image1, existingTags.get(1)),
			new ImageTag(image2, existingTags.get(0)),
			new ImageTag(image2, existingTags.get(1))
		);

		given(imageRepository.saveAll(anyList())).willReturn(imagesToSave);
		given(tagRepository.findByNameIn(anyList())).willReturn(existingTags);
		given(imageTagFactory.create(any(), anyList())).willReturn(expectedImageTags);
		given(imageTagRepository.saveAll(anyList())).willReturn(expectedImageTags);

		// when
		imageTaggingDomainService.registerNewImagesWithTags(imagesToSave, requestTagNames);

		// then
		ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
		then(tagRepository).should(atMost(1)).saveAll(captor.capture());

		assertThat(captor.getValue()).isEmpty();
	}

	@Test
	void 일부_태그는_신규_일부_태그는_기존일_때_이미지와_태그를_성공적으로_등록해야_한다() {
		// given
		List<String> requestTagNames = List.of("existingTag1", "newTag3");
		Tag existingTag = new Tag(1L, "existingTag1");
		Tag newTag = new Tag(2L, "newTag3");

		List<Tag> retrievedExistingTags = List.of(existingTag);
		List<Tag> newTagsToSave = List.of(newTag);

		List<ImageTag> expectedImageTags = List.of(
			new ImageTag(image1, existingTag),
			new ImageTag(image1, newTag),
			new ImageTag(image2, existingTag),
			new ImageTag(image2, newTag)
		);

		given(imageRepository.saveAll(anyList())).willReturn(imagesToSave);
		given(tagRepository.findByNameIn(anyList())).willReturn(retrievedExistingTags);
		given(tagRepository.saveAll(anyList())).willReturn(newTagsToSave);
		given(imageTagFactory.create(any(), anyList())).willReturn(expectedImageTags);
		given(imageTagRepository.saveAll(anyList())).willReturn(expectedImageTags);

		// when
		imageTaggingDomainService.registerNewImagesWithTags(imagesToSave, requestTagNames);

		// then
		then(tagRepository).should(times(1)).saveAll(anyList());
	}
}

