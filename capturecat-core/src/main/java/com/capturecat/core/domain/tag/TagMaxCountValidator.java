package com.capturecat.core.domain.tag;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Component
public class TagMaxCountValidator {

    private static final int TAG_MAX_COUNT = 4;

    public void validate(Set<String> existingTagNames, List<String> newTagNames) {
        long uniqueNewTagsCount = newTagNames.stream()
                .filter(tag -> !existingTagNames.contains(tag))
                .distinct()
                .count();

        if (existingTagNames.size() + uniqueNewTagsCount > TAG_MAX_COUNT) {
            throw new CoreException(ErrorType.TOO_MANY_TAGS);
        }
    }
}
