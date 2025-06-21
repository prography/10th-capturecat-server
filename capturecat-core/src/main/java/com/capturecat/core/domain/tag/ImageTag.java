package com.capturecat.core.domain.tag;

import com.capturecat.core.domain.BaseTimeEntity;
import com.capturecat.core.domain.image.Image;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ImageTag extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Image image;

	@ManyToOne(fetch = FetchType.LAZY)
	private Tag tag;

	public ImageTag(Image image, Tag tag) {
		this.image = image;
		this.tag = tag;
	}

}
