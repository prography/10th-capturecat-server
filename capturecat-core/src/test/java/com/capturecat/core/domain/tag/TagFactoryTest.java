package com.capturecat.core.domain.tag;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagFactoryTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagFactory tagFactory;

    private Tag tag1;
    private Tag tag2;
    private Tag tag3;

    @BeforeEach
    void setUp() {
        tag1 = new Tag("Java");
        tag2 = new Tag("Spring");
        tag3 = new Tag("Backend");
    }
    
    @Test
    void 기존_태그만_있는_경우_저장하지_않고_기존_태그를_반환한다() {
        // given
        List<String> tagNames = List.of("Java", "Spring");

        given(tagRepository.findByNameIn(anyList())).willReturn(List.of(tag1, tag2));
        given(tagRepository.saveAll(anyList())).willReturn(Collections.emptyList());
        
        // when
        List<Tag> result = tagFactory.create(tagNames);
        
        // then
        verify(tagRepository).saveAll(anyList());
        assertThat(result).hasSize(tagNames.size())
                .containsExactlyInAnyOrder(tag1, tag2);
    }

    @Test
    void 일부_태그는_기존에_있고_일부_태그는_새로_생성되는_경우_모든_태그를_반환한다() {
        // given
        List<String> tagNames = List.of("Java", "Spring", "Backend");

        given(tagRepository.findByNameIn(anyList())).willReturn(List.of(tag1, tag2));
        given(tagRepository.saveAll(anyList())).willReturn(Collections.singletonList(tag3));

        // when
        List<Tag> result = tagFactory.create(tagNames);

        // then
        verify(tagRepository).saveAll(anyList());
        assertThat(result).hasSize(3)
                .containsExactlyInAnyOrder(tag1, tag2, tag3);
    }

    @Test
    void 모든_태그가_새로_생성되는_경우_생성된_모든_태그를_반환한다() {
        // given
        List<String> tagNames = List.of("Java", "Spring", "Backend");

        given(tagRepository.findByNameIn(anyList())).willReturn(Collections.emptyList());
        given(tagRepository.saveAll(anyList())).willReturn(List.of(tag1, tag2, tag3));

        // when
        List<Tag> result = tagFactory.create(tagNames);

        // then
        verify(tagRepository).saveAll(anyList());
        assertThat(result).hasSize(3)
                .containsExactlyInAnyOrder(tag1, tag2, tag3);
    }
}
