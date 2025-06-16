package com.capturecat.core.api.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** 이미지 관련 응답 DTO */
public class ImageRespDto {

    /** 이미지 정보 리스트 응답 DTO */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ImageListDto {
        private List<ImageDto> images;
    }

    /** 개별 이미지 정보 DTO */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ImageDto {
        private Long id;
        private String fileName;
        private String fileUrl;
        private Long size;
        private String createdDate;
        private String lastModifiedDate;
    }
}
