package com.capturecat.core.support.querydsl;

import static com.capturecat.core.domain.image.QImage.image;
import static com.capturecat.core.domain.tag.QImageTag.imageTag;
import static com.capturecat.core.domain.tag.QTag.tag;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonTagQueryConditions {

	public static BooleanBuilder createExistsCondition(List<String> tagNames) {
		BooleanBuilder allTagsExistBuilder = new BooleanBuilder();
		for (String tagName : tagNames) {
			allTagsExistBuilder.and(JPAExpressions.selectOne()
				.from(imageTag)
				.join(imageTag.tag, tag)
				.where(imageTag.image.id.eq(image.id), tag.name.eq(tagName))
				.exists()
			);
		}
		return allTagsExistBuilder;
	}
}
