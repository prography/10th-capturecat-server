package com.capturecat.core.domain.bookmark;

import static com.capturecat.core.domain.bookmark.QBookmark.bookmark;
import static com.capturecat.core.domain.image.QImage.image;
import static com.capturecat.core.domain.tag.QImageTag.imageTag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.tag.QTag;
import com.capturecat.core.domain.tag.Tag;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class BookmarkCustomRepositoryImpl implements BookmarkCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Bookmark> searchBookmarksByUser(User user, Tag tag, Pageable pageable) {
		List<Bookmark> bookmarks = queryFactory
			.selectFrom(bookmark)
			.join(bookmark.image, image).fetchJoin()
			.leftJoin(imageTag).on(imageTag.image.eq(image))
			.leftJoin(QTag.tag).on(imageTag.tag.eq(QTag.tag))
			.where(bookmark.user.eq(user), eqTag(tag))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.orderBy(bookmark.id.desc())
			.fetch();

		return SliceUtil.toSlice(bookmarks, pageable);
	}

	private BooleanExpression eqTag(Tag tag) {
		if (tag == null) {
			return null;
		}
		return imageTag.tag.eq(tag);
	}
}
