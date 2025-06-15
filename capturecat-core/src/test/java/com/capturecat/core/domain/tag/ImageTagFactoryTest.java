package com.capturecat.core.domain.tag;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capturecat.core.domain.image.Image;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageTagFactoryTest {

    @Mock
    private ImageTagRepository imageTagRepository;
    
    @InjectMocks
    private ImageTagFactory imageTagFactory;

    private Image image;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        image = new Image();
        tag1 = new Tag("Java");
        tag2 = new Tag("Spring");
    }
    
    @Test
    void 모든_이미지_태그_조합이_존재하는_경우_저장하지_않는다() {
        // given
        List<Tag> tags = List.of(tag1, tag2);

        given(imageTagRepository.existsByImageAndTag(any(), any())).willReturn(true);
        given(imageTagRepository.saveAll(anyList())).willReturn(Collections.emptyList());
        
        // when
        imageTagFactory.create(image, tags);
        
        // then
        verify(imageTagRepository).saveAll(anyList());
    }

    @Test
    void 일부_이미지_태그_조합만_존재하는_경우_존재하지_않는_조합만_저장한다() {
        // given
        List<Tag> tags = List.of(tag1, tag2);

        given(imageTagRepository.existsByImageAndTag(image, tag1)).willReturn(true);
        given(imageTagRepository.existsByImageAndTag(image, tag2)).willReturn(false);
        given(imageTagRepository.saveAll(anyList())).willReturn(Collections.singletonList(new ImageTag(image, tag2)));

        // when
        imageTagFactory.create(image, tags);

        // then
        verify(imageTagRepository).saveAll(anyList());
    }

    @Test
    void 모든_이미지_태그_조합이_새로_생성되는_경우_모두_저장한다() {
        // given
        List<Tag> tags = List.of(tag1, tag2);

        given(imageTagRepository.existsByImageAndTag(any(), any())).willReturn(false);
        given(imageTagRepository.saveAll(anyList())).willReturn(List.of(new ImageTag(image, tag1), new ImageTag(image, tag2)));

        // when
        imageTagFactory.create(image, tags);

        // then
        verify(imageTagRepository).saveAll(anyList());
    }
}
