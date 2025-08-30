package com.capturecat.core.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;
import com.capturecat.core.domain.tag.Tag;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTag extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	private Tag tag;
}
