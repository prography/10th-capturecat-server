package com.capturecat.core.service.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.capturecat.core.domain.user.WithdrawLog;
import com.capturecat.core.domain.user.WithdrawLogRepository;

@Service
@RequiredArgsConstructor
public class WithdrawLogService {
	private final WithdrawLogRepository withdrawLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(Long userId, String reason) {
		WithdrawLog log = WithdrawLog.builder()
			.userId(userId)
			.reason(reason)
			.build();
		withdrawLogRepository.save(log);
	}
}
