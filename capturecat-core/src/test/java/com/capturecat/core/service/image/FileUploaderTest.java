package com.capturecat.core.service.image;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.client.upload.FileUploader;
import com.capturecat.core.domain.image.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private FileUploader fileUploader;

	@Mock
	private ImageRepository imageRepository;

	@Mock
	private ImageMapper imageMapper;

	// @Test
	// @DisplayName("이미지 업로드 성공(파일2개)")
	// void uploadImages_success() throws IOException {
	// 	// given
	// 	ClassPathResource resource1 = new ClassPathResource("images/spring1.jpg");
	// 	ClassPathResource resource2 = new ClassPathResource("images/spring2.jpg");
	//
	// 	MockMultipartFile file1 = new MockMultipartFile("files", "spring1.jpg", "image/jpeg",
	// 			resource1.getInputStream());
	// 	MockMultipartFile file2 = new MockMultipartFile("files", "spring2.jpg", "image/jpeg",
	// 			resource2.getInputStream());
	//
	// 	when(fileUploader.upload(any())).thenReturn("url");
	// 	when(imageRepository.saveAll(any())).thenReturn(List.of());
	//
	// 	// when
	// 	imageService.save(List.of(file1, file2));
	//
	// 	// then
	// 	verify(fileUploader, times(2)).upload(any());
	// 	verify(imageMapper, times(1)).toDto(anyList());
	// 	verify(imageRepository, times(1)).saveAll(any());
	// 	verifyNoMoreInteractions(fileUploader, imageMapper, imageRepository);
	// }

}
