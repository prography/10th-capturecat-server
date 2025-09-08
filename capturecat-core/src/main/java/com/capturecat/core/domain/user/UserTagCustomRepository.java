package com.capturecat.core.domain.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserTagCustomRepository {

	Slice<UserTag> findAllByUser(User user, Pageable pageable);
}
