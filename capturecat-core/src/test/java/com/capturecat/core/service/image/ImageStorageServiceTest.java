package com.capturecat.core.service.image;

import com.capturecat.core.domain.image.Image;
import com.capturecat.core.domain.image.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local") //"dev"로 변경 시 S3 테스트로 전환 가능
class ImageStorageServiceTest {

    @Autowired
    private ImageStorageService imageStorageService;
    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("이미지 업로드 성공(파일2개)")
    void uploadImages_success() throws IOException {
        //given
        ClassPathResource resource1 = new ClassPathResource("images/spring1.jpg");
        ClassPathResource resource2 = new ClassPathResource("images/spring2.jpg");

        MockMultipartFile file1 = new MockMultipartFile("files", "spring1.jpg", "image/jpeg", resource1.getInputStream());
        MockMultipartFile file2 = new MockMultipartFile("files", "spring2.jpg", "image/jpeg", resource2.getInputStream());

        //when
        List<Image> savedImages = imageStorageService.store(List.of(file1, file2));

        //then
        assertThat(savedImages).hasSize(2);
        //파일을 스토어에 저장 시 난수_파일명 형식
        assertThat(savedImages.get(0).getFileUrl()).contains("_" + file1.getOriginalFilename());
        assertThat(savedImages.get(1).getFileUrl()).contains("_" + file2.getOriginalFilename());
        //DB 저장 확인
        assertThat(imageRepository.findAll()).hasSize(2);
    }
}