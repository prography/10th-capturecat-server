package com.capturecat.core.api.image.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.capturecat.core.service.image.dto.ImageUploadItem;

@Setter
@Getter
public class ImageAndTagUploadRequest {

	private List<ImageUploadItem> uploadItems;
}
