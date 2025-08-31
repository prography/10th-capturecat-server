package com.capturecat.core.domain.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagRegisterTest {

	@Mock
	private TagRepository tagRepository;

	@InjectMocks
	private TagRegister tagRegister;

	@Test
	void 모든_요청_태그가_새로운_태그일_경우_모두_저장하고_반환한다() {
		// given
		List<String> tagNames = List.of("새태그1", "새태그2", "새태그3");
		List<Tag> expectedSavedTags = tagNames.stream()
			.map(Tag::new)
			.toList();

		given(tagRepository.findByNameIn(anyList())).willReturn(Collections.emptyList());
		given(tagRepository.saveAll(anyList())).willReturn(expectedSavedTags);

		// when
		List<Tag> resultTags = tagRegister.registerTagsFor(tagNames);

		// then
		assertThat(resultTags).hasSize(tagNames.size());
		assertThat(resultTags).extracting(Tag::getName)
			.containsExactlyInAnyOrderElementsOf(tagNames);
	}

	@Test
	void 일부_요청_태그가_존재하고_일부는_새로_생성될_경우_기존_태그는_조회하고_새_태그는_저장하여_반환한다() {
		// given
		List<String> requestedTagNames = List.of("기존태그1", "새태그1", "기존태그2", "새태그2");
		Tag existingTag1 = new Tag("기존태그1");
		Tag existingTag2 = new Tag("기존태그2");
		List<String> newTagNames = List.of("새태그1", "새태그2");
		List<Tag> expectedSavedNewTags = newTagNames.stream()
			.map(Tag::new)
			.toList();

		given(tagRepository.findByNameIn(anyList())).willReturn(List.of(existingTag1, existingTag2));
		given(tagRepository.saveAll(anyList())).willReturn(expectedSavedNewTags);

		// when
		List<Tag> resultTags = tagRegister.registerTagsFor(requestedTagNames);

		// then
		assertThat(resultTags).hasSize(requestedTagNames.size());
		assertThat(resultTags).extracting(Tag::getName)
			.containsExactlyInAnyOrderElementsOf(requestedTagNames);
	}

	@Test
	void 모든_요청_태그가_이미_존재할_경우_새로운_태그를_저장하지_않고_기존_태그만_반환한다() {
		// given
		List<String> tagNames = List.of("기존태그1", "기존태그2");
		Tag existingTag1 = new Tag("기존태그1");
		Tag existingTag2 = new Tag("기존태그2");

		given(tagRepository.findByNameIn(anyList())).willReturn(List.of(existingTag1, existingTag2));

		// when
		List<Tag> resultTags = tagRegister.registerTagsFor(tagNames);

		// then
		verify(tagRepository, times(1)).findByNameIn(tagNames);
		verify(tagRepository, times(1)).saveAll(Collections.emptyList());
		assertThat(resultTags).hasSize(tagNames.size());
		assertThat(resultTags).extracting(Tag::getName)
			.containsExactlyInAnyOrderElementsOf(tagNames);
	}

	@Test
	void 태그가_저장되지_않은_경우_태그를_저장한다() {
		// given
		String tagName = "단일태그";

		given(tagRepository.findByName(tagName)).willReturn(Optional.empty());
		given(tagRepository.save(any())).willReturn(TagFixture.createTag(1L, tagName));

		// when
		Tag resultTag = tagRegister.registerTagsFor(tagName);

		// then
		assertThat(resultTag.getId()).isNotNull();
		assertThat(resultTag.getName()).isEqualTo(tagName);

		verify(tagRepository, times(1)).findByName(tagName);
		verify(tagRepository, times(1)).save(any());
	}

	@Test
	void 이미_저장된_태그인_경우_태그_엔티티를_반환한다() {
		// given
		String tagName = "기존태그";

		given(tagRepository.findByName(tagName)).willReturn(Optional.of(TagFixture.createTag(1L, tagName)));

		// when
		Tag resultTag = tagRegister.registerTagsFor(tagName);

		// then
		assertThat(resultTag.getId()).isNotNull();
		assertThat(resultTag.getName()).isEqualTo(tagName);

		verify(tagRepository, times(1)).findByName(tagName);
		verify(tagRepository, never()).save(any());
	}
}
