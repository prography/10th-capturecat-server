package com.capturecat.core.domain.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
	Optional<UserSocialAccount> findUserByProviderAndSocialId(String provider, String socialId);

	List<UserSocialAccount> findByUser(User user);
}
