package com.capturecat.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigTest {

	@Autowired
	RoleHierarchy roleHierarchy;

	@Test
	void 관리자권한은_회원권한을_포함한다() {
		//given
		List<GrantedAuthority> adminAuthority = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

		//when
		Collection<? extends GrantedAuthority> reachableGrantedAuthorities =
			roleHierarchy.getReachableGrantedAuthorities(adminAuthority);

		//then
		Assertions.assertThat(reachableGrantedAuthorities).extracting(GrantedAuthority::getAuthority)
			.containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PREMIUM_USER", "ROLE_USER");
	}
}
