package com.capturecat.core.domain.tag;

import static com.capturecat.core.domain.bookmark.QBookmark.bookmark;
import static com.capturecat.core.domain.image.QImage.image;
import static com.capturecat.core.domain.tag.QImageTag.imageTag;
import static com.capturecat.core.domain.tag.QTag.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.user.QUser;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.querydsl.CommonTagQueryConditions;
import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class TagCustomRepositoryImpl implements TagCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Tag> searchUserTagsByUser(User user, Pageable pageable) {
		List<Tag> tags = queryFactory
			.select(tag)
			.from(imageTag)
			.join(imageTag.image, image)
			.join(imageTag.tag, tag)
			.where(image.user.eq(user))
			.orderBy(imageTag.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return SliceUtil.toSlice(tags, pageable);
	}

	@Override
	public Slice<Tag> searchByRelatedTags(User user, List<String> tagNames, Pageable pageable) {
		List<Tag> tags = queryFactory
			.select(tag)
			.from(imageTag)
			.join(imageTag.image, image)
			.join(imageTag.tag, tag)
			.where(
				image.user.eq(user),
				CommonTagQueryConditions.createExistsCondition(tagNames),
				tag.name.notIn(tagNames)
			)
			.groupBy(tag)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return SliceUtil.toSlice(tags, pageable);
	}

	@Override
	public Slice<Tag> searchMostUsedTagsByUser(User user, Pageable pageable) {
		List<Tag> tags = queryFactory
			.select(tag)
			.from(imageTag)
			.join(imageTag.tag, tag)
			.join(imageTag.image, image)
			.where(image.user.eq(user))
			.groupBy(tag)
			.orderBy(imageTag.count().desc(), tag.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return SliceUtil.toSlice(tags, pageable);
	}

	@Override
	public List<Tag> searchByKeyword(String keyword, Long userId, int size) {
		return queryFactory
			.select(tag)
			.distinct()
			.from(imageTag)
			.join(imageTag.tag, tag)
			.join(imageTag.image, image)
			.where(image.user.id.eq(userId), tag.name.startsWithIgnoreCase(keyword))
			.limit(size)
			.fetch();
	}

	@Override
	public Slice<Tag> searchTagsByMemberBookmark(User user, Pageable pageable) {
		List<Tag> content = queryFactory
			.select(tag)
			.from(imageTag)
			.join(imageTag.tag, tag)
			.join(imageTag.image, image)
			.join(bookmark).on(bookmark.image.eq(image))
			.join(bookmark.user, QUser.user)
			.where(QUser.user.eq(user))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.orderBy(imageTag.createdDate.desc())
			.fetch();

		return SliceUtil.toSlice(content, pageable);
	}
}
