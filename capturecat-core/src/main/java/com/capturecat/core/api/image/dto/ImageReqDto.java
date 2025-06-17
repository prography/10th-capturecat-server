package com.capturecat.core.api.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 이미지 관련 요청 DTO */
public class ImageReqDto {

    /** 태그로 이미지 검색 요청 DTO */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ImageSearchByUserDto {
        private Long userId;
        private String createdDate;
        private String lastModifiedDate;
    }
}
