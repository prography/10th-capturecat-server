package com.capturecat.core.domain.image;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

	@Id
	@GeneratedValue
	private Long id;

	private String fileName;

	private String fileUrl;

	private Long size;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	// todo : createdby, modifiedby 설정

	@Builder
	public Image(Long id, String fileName, String fileUrl, long size) {
		this.id = id;
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.size = size;
	}

}
