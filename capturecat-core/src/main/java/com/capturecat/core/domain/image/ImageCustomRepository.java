package com.capturecat.core.domain.image;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.capturecat.core.domain.image.dto.ImageInfo;
import com.capturecat.core.domain.user.User;

public interface ImageCustomRepository {

	Slice<ImageInfo> searchByUser(User user, Pageable pageable);

	Slice<ImageInfo> searchImagesByUserAndTagNames(User user, List<String> tagNames, Pageable pageable);
}
