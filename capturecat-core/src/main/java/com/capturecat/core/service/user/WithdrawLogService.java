package com.capturecat.core.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.capturecat.core.domain.user.WithdrawLog;
import com.capturecat.core.domain.user.WithdrawLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawLogService {
	private final WithdrawLogRepository withdrawLogRepository;

	// 전파 시 실패해도 록백하지 않도록
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(Long userId, String reason) {
		try {
			WithdrawLog log = WithdrawLog.builder()
				.userId(userId)
				.reason(reason)
				.build();
			withdrawLogRepository.save(log);
		} catch (Exception e) {
			log.error("Failed to persist withdraw log. userId={}, reason={}", userId, reason, e);
		}

	}
}
