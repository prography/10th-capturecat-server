package com.capturecat.core.domain.tag;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
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
}

