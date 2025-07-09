package com.capturecat.core.domain.bookmark;

import static com.capturecat.core.domain.bookmark.QBookmark.bookmark;
import static com.capturecat.core.domain.image.QImage.image;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class BookmarkCustomRepositoryImpl implements BookmarkCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Bookmark> searchBookmarksByUser(User user, Pageable pageable) {
		List<Bookmark> bookmarks = queryFactory
			.selectFrom(bookmark)
			.join(bookmark.image, image).fetchJoin()
			.where(bookmark.user.eq(user))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.orderBy(bookmark.id.desc())
			.fetch();

		return SliceUtil.toSlice(bookmarks, pageable);
	}
}
