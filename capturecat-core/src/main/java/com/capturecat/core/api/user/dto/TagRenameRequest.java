package com.capturecat.core.api.user.dto;

public record TagRenameRequest(Long currentTagId, String newTagName) {
}
