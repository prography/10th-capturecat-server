package com.capturecat.core.domain.tag;

import static com.capturecat.core.domain.image.QImage.image;
import static com.capturecat.core.domain.tag.QImageTag.imageTag;
import static com.capturecat.core.domain.tag.QTag.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class TagCustomRepositoryImpl implements TagCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Tag> searchUserTagsByUser(User user, Pageable pageable) {
		List<Tag> tags = queryFactory
			.selectFrom(tag)
			.leftJoin(imageTag).on(imageTag.tag.eq(tag))
			.leftJoin(image).on(imageTag.tag.eq(tag))
			.where(image.user.eq(user))
			.orderBy(tag.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return SliceUtil.toSlice(tags, pageable);
	}
}
