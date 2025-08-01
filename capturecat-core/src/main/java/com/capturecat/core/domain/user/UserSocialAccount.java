package com.capturecat.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Entity
@Table(name = "user_social_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id", callSuper = false)
public class UserSocialAccount extends BaseTimeEntity {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 30)
	private String provider;    // "google", "apple", "kakao" 등

	@Column(nullable = false, length = 100)
	private String socialId;    // sub. 카카오의 경우 userId와 같은 값

	@Column(length = 512)
	private String unlinkKey; // provider별 unlink/revoke에 필요한 값만 저장

	@Builder
	public UserSocialAccount(User user, String provider, String socialId, String unlinkKey) {
		this.user = user;
		this.provider = provider;
		this.socialId = socialId;
		this.unlinkKey = unlinkKey;
	}
}
