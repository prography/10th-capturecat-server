package com.capturecat.core.domain.image;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor
public class Image extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String fileName;

	private String fileUrl;

	private Long size;

	// todo : user 정보 매핑

	@Builder
	public Image(Long id, String fileName, String fileUrl, long size) {
		this.id = id;
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.size = size;
	}

}
