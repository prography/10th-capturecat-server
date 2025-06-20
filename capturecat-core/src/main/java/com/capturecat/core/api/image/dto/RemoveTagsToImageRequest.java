package com.capturecat.core.api.image.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RemoveTagsToImageRequest(@NotEmpty(message = "잘못된 요청입니다.")
                                       @NotNull(message = "잘못된 요청입니다.")
                                       List<Long> tagIds) {
}
