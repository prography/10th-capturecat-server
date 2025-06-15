package com.capturecat.core.domain.tag;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TagFactory {

    private final TagRepository tagRepository;

    @Transactional
    public List<Tag> create(List<String> tagNames) {
        List<Tag> existedTags = tagRepository.findByNameIn(tagNames);
        List<Tag> notExistedTags = tagNames.stream()
                .filter(tagName -> existedTags.stream().noneMatch(t -> t.isSameNameAs(tagName)))
                .map(Tag::new)
                .toList();
        List<Tag> createdTags = tagRepository.saveAll(notExistedTags);

        return Stream.concat(existedTags.stream(), createdTags.stream())
                .toList();
    }
}
