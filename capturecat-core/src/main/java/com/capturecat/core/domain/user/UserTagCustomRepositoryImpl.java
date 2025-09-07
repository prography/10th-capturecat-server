package com.capturecat.core.domain.user;

import static com.capturecat.core.domain.user.QUserTag.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class UserTagCustomRepositoryImpl implements UserTagCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<UserTag> findAllByUser(User user, Pageable pageable) {
		List<UserTag> userTags = queryFactory
			.selectFrom(userTag)
			.leftJoin(userTag.tag).fetchJoin()
			.where(userTag.user.eq(user))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.orderBy(userTag.id.desc())
			.fetch();

		return SliceUtil.toSlice(userTags, pageable);
	}
}
