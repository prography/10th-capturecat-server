package com.capturecat.core.domain.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.capturecat.core.domain.BaseTimeEntity;

@Getter
@Entity
@Table(name = "withdraw_log",
	indexes = {
		@Index(name = "idx_withdraw_log_user_id", columnList = "user_id"),
		@Index(name = "idx_withdraw_log_created_date", columnList = "created_date"),
		@Index(name = "idx_withdraw_log_s3_status", columnList = "s3_cleanup_status, created_date")
	})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WithdrawLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(columnDefinition = "text")
	private String reason;

	/** S3 삭제 배치 처리 상태 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private S3CleanupStatus s3CleanupStatus = S3CleanupStatus.PENDING;


	public void markDone() {
		this.s3CleanupStatus = S3CleanupStatus.DONE;
	}

	public void markFailed() {
		this.s3CleanupStatus = S3CleanupStatus.FAILED;
	}
}
