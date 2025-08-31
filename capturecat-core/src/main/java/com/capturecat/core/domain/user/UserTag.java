package com.capturecat.core.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;
import com.capturecat.core.domain.tag.Tag;

@Entity
@Table(name = "user_tag",
	uniqueConstraints = @UniqueConstraint(name = "uk_user_tag_user_tag", columnNames = {"user_id", "tag_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserTag extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id")
	private Tag tag;

	private UserTag(User user, Tag tag) {
		this.user = user;
		this.tag = tag;
	}

	public static UserTag create(User user, Tag tag) {
		return new UserTag(user, tag);
	}
}
