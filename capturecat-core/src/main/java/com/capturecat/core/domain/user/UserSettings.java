package com.capturecat.core.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSettings extends BaseTimeEntity {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "screenshot_auto_delete_enabled", nullable = false)
	private boolean screenshotAutoDeleteEnabled = false;


	public static UserSettings init(Long userId) {
		UserSettings settings = new UserSettings();
		settings.userId = userId;
		settings.screenshotAutoDeleteEnabled = false;
		return settings;
	}

	public void changeAutoDelete(boolean enabled) {
		this.screenshotAutoDeleteEnabled = enabled;
	}
}
