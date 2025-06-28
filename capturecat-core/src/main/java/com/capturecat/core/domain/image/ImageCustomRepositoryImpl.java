package com.capturecat.core.domain.image;

import static com.capturecat.core.domain.image.QImage.image;
import static com.capturecat.core.domain.tag.QImageTag.imageTag;
import static com.capturecat.core.domain.tag.QTag.tag;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.image.dto.ImageInfo;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.util.SliceUtil;

@RequiredArgsConstructor
public class ImageCustomRepositoryImpl implements ImageCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<ImageInfo> searchByUser(User user, Pageable pageable) {
		List<ImageInfo> responses = queryFactory
			.selectFrom(image)
			.where(image.user.eq(user))
			.leftJoin(imageTag).on(image.id.eq(imageTag.image.id))
			.leftJoin(tag).on(imageTag.tag.id.eq(tag.id))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.orderBy(image.id.desc())
			.transform(GroupBy.groupBy(image.id).list(
				Projections.constructor(ImageInfo.class,
					image.id,
					image.fileName,
					image.fileUrl,
					GroupBy.list(tag))
				));

		return SliceUtil.toSlice(responses, pageable);
	}
}
