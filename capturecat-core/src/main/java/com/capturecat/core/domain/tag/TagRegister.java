package com.capturecat.core.domain.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.annotation.DomainService;

@DomainService
@RequiredArgsConstructor
public class TagRegister {

	private final TagRepository tagRepository;

	/**
	 * 등록되지 않은 태그들을 등록하고, 이미 존재하는 태그들은 그대로 반환합니다.
	 * @param tagNames 태그 이름들의 리스트
	 * @return 조회되거나 새로 생성된 모든 {@link Tag} 엔티티의 목록. 반환되는 리스트는 입력된 tagNames의 순서를 보장하지 않습니다.
	 */
	@Transactional
	public List<Tag> registerTagsFor(List<String> tagNames) {
		Map<String, Tag> existingTagsByName = tagRepository.findByNameIn(tagNames).stream()
			.collect(Collectors.toMap(Tag::getName, Function.identity()));

		List<Tag> newTags = tagNames.stream()
			.filter(tagName -> !existingTagsByName.containsKey(tagName))
			.map(Tag::new)
			.toList();

		List<Tag> savedNewTags = tagRepository.saveAll(newTags);
		List<Tag> result = new ArrayList<>(existingTagsByName.values());
		result.addAll(savedNewTags);
		return result;
	}
}
