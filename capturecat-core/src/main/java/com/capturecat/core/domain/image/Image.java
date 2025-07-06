package com.capturecat.core.domain.image;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;
import com.capturecat.core.domain.user.User;
import com.capturecat.core.support.error.CoreException;
import com.capturecat.core.support.error.ErrorType;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fileName;

	private String fileUrl;

	private Long size;

	private LocalDate captureDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	// todo : createdby, modifiedby 설정

	@Builder
	public Image(Long id, String fileName, String fileUrl, long size, LocalDate captureDate, User user) {
		this.id = id;
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.size = size;
		this.captureDate = captureDate;
		this.user = user;
	}

	public void validateOwnership(User user) {
		if (isNotOwnedBy(user)) {
			throw new CoreException(ErrorType.IMAGE_ACCESS_DENIED);
		}
	}

	public boolean isSameFileNameAs(String fileName) {
		return this.fileName.equals(fileName);
	}

	private boolean isNotOwnedBy(User user) {
		return !this.user.equals(user);
	}
}
