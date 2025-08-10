package com.capturecat.core.domain.user;

public enum S3CleanupStatus {
	PENDING,   // 기본값: 탈퇴 직후
	DONE,      // 배치가 정상 처리
	FAILED     // 배치 실패(재시도 대상)
}
