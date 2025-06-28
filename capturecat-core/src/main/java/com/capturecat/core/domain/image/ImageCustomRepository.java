package com.capturecat.core.domain.image;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.service.image.ImageWithTagsResponse;

public interface ImageCustomRepository {

	Slice<ImageWithTagsResponse> searchByUser(User user, Pageable pageable);
}
